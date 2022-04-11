package moreid.asm;

import io.netty.buffer.Unpooled;
import moreid.Config;
import moreid.MoreId;
import moreid.asm.registry.BlockCallbacks;
import moreid.util.Reflects;
import roj.asm.nixim.Copy;
import roj.asm.nixim.Inject;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import net.minecraftforge.fml.common.FMLLog;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Roj234
 * @since 2021/2/2 16:26
 */
@Nixim("net.minecraft.world.chunk.storage.AnvilChunkLoader")
abstract class Nx_MIChunkLoader extends AnvilChunkLoader {
    @Copy(staticInitializer = "initCache", targetIsFinal = true)
    static ThreadLocal<PacketBuffer[]> myCache;

    static void initCache() {
        myCache = new ThreadLocal<>();
    }

    @Shadow("field_75828_a")
    private Map<ChunkPos, NBTTagCompound> chunksToSave;

    @Shadow("field_193415_c")
    private Set<ChunkPos> chunksBeingSaved;

    @Shadow("field_193416_e")
    private DataFixer fixer;

    public Nx_MIChunkLoader(File file, DataFixer fixer) {
        super(file, fixer);
    }

    @Copy
    PacketBuffer[] getBuf() {
        PacketBuffer[] o = myCache.get();
        if(o == null) {
            myCache.set(o = new PacketBuffer[] {
                    new PacketBuffer(Unpooled.buffer(0)),
                    new PacketBuffer(Unpooled.buffer(1024))
            });
        }

        return o;
    }

    @Inject("func_75820_a")
    private void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound tag) {
        if (world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) return;
        tag.setInteger("xPos", chunk.x);
        tag.setInteger("zPos", chunk.z);
        tag.setLong("LastUpdate", world.getTotalWorldTime());
        tag.setIntArray("HeightMap", chunk.getHeightMap());
        tag.setBoolean("TerrainPopulated", chunk.isTerrainPopulated());
        tag.setBoolean("LightPopulated", chunk.isLightPopulated());
        tag.setLong("InhabitedTime", chunk.getInhabitedTime());
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        NBTTagList section = new NBTTagList();
        
        boolean flag = world.provider.hasSkyLight();
        int len = sections.length;

        NBTTagCompound sectTag;
        for (ExtendedBlockStorage currSect : sections) {
            if (currSect != Chunk.NULL_BLOCK_STORAGE) {
                sectTag = new NBTTagCompound();
                sectTag.setByte("Y", (byte) (currSect.getYLocation() >> 4));

                if (Config.useNewChunkFormat) {
                    saveNewFormat(currSect, sectTag, chunk);
                } else {
                    saveOldFormat(currSect, sectTag);
                }

                sectTag.setByteArray("BlockLight", currSect.getBlockLight().getData());
                if (flag) {
                    sectTag.setByteArray("SkyLight", currSect.getSkyLight().getData());
                } else if(!Config.useNewChunkFormat) {
                    sectTag.setByteArray("SkyLight", new byte[2048]);
                }

                section.appendTag(sectTag);
            }
        }

        tag.setTag("Sections", section);
        tag.setByteArray("Biomes", chunk.getBiomeArray());
        chunk.setHasEntities(false);
        NBTTagList entitites = new NBTTagList();

        for(int i = 0; i < chunk.getEntityLists().length; ++i) {
            for(Entity entity : chunk.getEntityLists()[i]) {
                NBTTagCompound entityTag = new NBTTagCompound();

                try {
                    if (entity.writeToNBTOptional(entityTag)) {
                        chunk.setHasEntities(true);
                        entitites.appendTag(entityTag);
                    }
                } catch (Exception e) {
                    FMLLog.log.error("An Entity type {} has thrown an exception trying to write state. It will not persist. Report this to the mod author", entity.getClass().getName(), e);
                }
            }
        }
        tag.setTag("Entities", entitites);

        NBTTagList tileEntities = new NBTTagList();
        for(TileEntity tile : chunk.getTileEntityMap().values()) {
            try {
                sectTag = tile.writeToNBT(new NBTTagCompound());
                tileEntities.appendTag(sectTag);
            } catch (Exception e) {
                FMLLog.log.error("A TileEntity type {} has throw an exception trying to write state. It will not persist. Report this to the mod author", tile.getClass().getName(), e);
            }
        }
        tag.setTag("TileEntities", tileEntities);

        List<NextTickListEntry> list = world.getPendingBlockUpdates(chunk, false);
        if (list != null) {
            long j = world.getTotalWorldTime();
            NBTTagList tileTicks = new NBTTagList();

            for(NextTickListEntry entry : list) {
                NBTTagCompound nextTick = new NBTTagCompound();
                ResourceLocation blockName = Block.REGISTRY.getNameForObject(entry.getBlock());
                nextTick.setString("i", blockName.toString());
                nextTick.setInteger("x", entry.position.getX());
                nextTick.setInteger("y", entry.position.getY());
                nextTick.setInteger("z", entry.position.getZ());
                nextTick.setInteger("t", (int)(entry.scheduledTime - j));
                nextTick.setInteger("p", entry.priority);
                tileTicks.appendTag(nextTick);
            }

            tag.setTag("TileTicks", tileTicks);
        }

        if (chunk.getCapabilities() != null) {
            try {
                tag.setTag("ForgeCaps", chunk.getCapabilities().serializeNBT());
            } catch (Exception e) {
                FMLLog.log.error("A capability provider has thrown an exception trying to write state. It will not persist. Report this to the mod author", e);
            }
        }
    }

    @Inject("checkedReadChunkFromNBT__Async")
    protected Object[] checkedReadChunkFromNBT__Async(World world, int x, int z, NBTTagCompound tag) {
        if (world.getWorldType() == WorldType.DEBUG_ALL_BLOCK_STATES) return null;

        if (!tag.hasKey("Level", 10)) {
            MoreId.logger().error("Chunk file at "+x+","+z+" is missing level data, skipping");
            return null;
        } else {
            NBTTagCompound levelTag = tag.getCompoundTag("Level");
            if (!levelTag.hasKey("Sections", 9)) {
                MoreId.logger().error("Chunk file at "+x+","+z+" is missing block data, skipping");
                return null;
            } else {
                Chunk chunk = this.readChunkFromNBT(world, levelTag);
                if (!chunk.isAtLocation(x, z)) {
                    MoreId.logger().error("区块 "+x+","+z+" 位置不对. (得到 "+chunk.x+","+chunk.z);
                    levelTag.setInteger("xPos", x);
                    levelTag.setInteger("zPos", z);
                    NBTTagList _tileEntities = levelTag.getTagList("TileEntities", 10);
                    for(int te = 0; te < _tileEntities.tagCount(); ++te) {
                        NBTTagCompound tileTag = _tileEntities.getCompoundTagAt(te);
                        tileTag.setInteger("x", x * 16 + (tileTag.getInteger("x") - chunk.x * 16));
                        tileTag.setInteger("z", z * 16 + (tileTag.getInteger("z") - chunk.z * 16));
                    }

                    chunk = this.readChunkFromNBT(world, levelTag);
                }

                return new Object[]{chunk, tag};
            }
        }
    }

    @Inject("func_75823_a")
    public Chunk readChunkFromNBT(World world, NBTTagCompound chunkTag) {
        Chunk chunk = new Chunk(world, chunkTag.getInteger("xPos"), chunkTag.getInteger("zPos"));
        chunk.setHeightMap(chunkTag.getIntArray("HeightMap"));
        chunk.setTerrainPopulated(chunkTag.getBoolean("TerrainPopulated"));
        chunk.setLightPopulated(chunkTag.getBoolean("LightPopulated"));
        chunk.setInhabitedTime(chunkTag.getLong("InhabitedTime"));
        NBTTagList section = chunkTag.getTagList("Sections", 10);
        
        ExtendedBlockStorage[] sections = new ExtendedBlockStorage[16];
        boolean flag = world.provider.hasSkyLight();

        for(int i = 0; i < section.tagCount(); i++) {
            NBTTagCompound sect = section.getCompoundTagAt(i);
            int y = sect.getByte("Y");
            ExtendedBlockStorage currSect = new ExtendedBlockStorage(y << 4, flag);
            
            if(sect.hasKey("Blocks")) {
                loadOldFormat(currSect, sect);
            } else {
                loadLatestFormat(currSect, sect, chunk);
            }
            
            currSect.setBlockLight(new NibbleArray(sect.getByteArray("BlockLight")));
            if (flag && sect.hasKey("SkyLight", 7)) {
                currSect.setSkyLight(new NibbleArray(sect.getByteArray("SkyLight")));
            }

            currSect.recalculateRefCounts();
            sections[y] = currSect;
        }

        chunk.setStorageArrays(sections);
        if (chunkTag.hasKey("Biomes", 7)) {
            chunk.setBiomeArray(chunkTag.getByteArray("Biomes"));
        }

        if (chunk.getCapabilities() != null && chunkTag.hasKey("ForgeCaps")) {
            chunk.getCapabilities().deserializeNBT(chunkTag.getCompoundTag("ForgeCaps"));
        }

        return chunk;
    }

    @Copy
    public static void loadOldFormat(ExtendedBlockStorage storage, NBTTagCompound tag) {
        BlockStateContainer c = storage.getData();

        byte[] blockId = tag.getByteArray("Blocks");
        NibbleArray blockData = new NibbleArray(tag.getByteArray("Data"));
        NibbleArray additionData = tag.hasKey("Add", 7) ? new NibbleArray(tag.getByteArray("Add")) : null;
        for(int i = 0; i < 4096; i++) {
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            int add = additionData == null ? 0 : additionData.get(x, y, z);
            int id = add << 12 | (blockId[i] & 255) << 4 | blockData.get(x, y, z);
            c.set(i, BlockCallbacks.STATE_ID.getByValue(id));
        }
    }

    @Copy
    public void loadLatestFormat(ExtendedBlockStorage storage, NBTTagCompound tag, Chunk chunk) {
        BlockStateContainer c = storage.getData();

        PacketBuffer rb = getBuf()[0];

        byte[] data = tag.getByteArray("Arr");
        Reflects.INSTANCE.setArray(rb.clear(), data);
        rb.writerIndex(data.length);
        c.read(rb);
    }

    @Copy
    public static void saveOldFormat(ExtendedBlockStorage storage, NBTTagCompound tag) {
        BlockStateContainer c = storage.getData();

        byte[] blocks = new byte[4096];
        NibbleArray stateData = new NibbleArray();
        NibbleArray additionalData = null;
        for(int i = 0; i < 4096; ++i) {
            IBlockState state = c.get(i);
            int id = BlockCallbacks.STATE_ID.get(state);
            int x = i & 15;
            int y = i >> 8 & 15;
            int z = i >> 4 & 15;
            if ((id >> 12 & 15) != 0) {
                if (additionalData == null) {
                    additionalData = new NibbleArray();
                }

                additionalData.set(x, y, z, id >> 12 & 15);
            }

            blocks[i] = (byte)(id >> 4 & 255);
            stateData.set(x, y, z, id & 15);
        }
        tag.setByteArray("Blocks", blocks);
        tag.setByteArray("Data", stateData.getData());
        if (additionalData != null) {
            tag.setByteArray("Add", additionalData.getData());
        }
    }

    @Copy
    public void saveNewFormat(ExtendedBlockStorage storage, NBTTagCompound tag, Chunk chunk) {
        BlockStateContainer c = storage.getData();

        PacketBuffer wb = getBuf()[1];

        wb.clear();
        c.write(wb);

        byte[] dst = new byte[wb.writerIndex()];
        wb.getBytes(0, dst);

        tag.setByteArray("Arr", dst);
    }
}
