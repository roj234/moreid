package moreid.asm.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.registries.GameData.ClearableObjectIntIdentityMap;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;

/**
 * @author Roj234
 * @since 2021/4/21 22:51
 */
public class BlockCallbacks implements IForgeRegistry.AddCallback<Block>, IForgeRegistry.ClearCallback<Block>, IForgeRegistry.CreateCallback<Block>, IForgeRegistry.DummyFactory<Block> {

    public static ClearableObjectIntIdentityMap<IBlockState> STATE_ID;

    static ResourceLocation BLOCK_TO_ITEM = new ResourceLocation("blocktoitemmap");
    static ResourceLocation BLOCKSTATE_TO_ID = new ResourceLocation("blockstatetoid");

    @SuppressWarnings("unchecked")
    public void onAdd(IForgeRegistryInternal<Block> owner, RegistryManager stage, int id, Block block, Block oldBlock) {
        ClearableObjectIntIdentityMap<IBlockState> map = owner.getSlaveMap(BLOCKSTATE_TO_ID, ClearableObjectIntIdentityMap.class);

        if (oldBlock != null) {
            for (IBlockState state : oldBlock.getBlockState().getValidStates()) {
                map.remove(state);
            }

            BiMap<Block, Item> blockToItem = owner.getSlaveMap(BLOCK_TO_ITEM, BiMap.class);
            Item item = blockToItem.get(oldBlock);
            if (item != null) {
                blockToItem.forcePut(block, item);
            }
        }

        id <<= 4;
        int metas = 0;
        for (IBlockState state : block.getBlockState().getValidStates()) {
            int meta = block.getMetaFromState(state);
            metas |= 1 << meta;

            map.put(state, id | meta);
        }

        int meta = 0;
        while (metas != 0) {
            if ((metas & 1) != 0) {
                map.put(block.getStateFromMeta(meta), id | meta);
            }
            metas >>>= 1;
            meta++;
        }
    }

    public void onClear(IForgeRegistryInternal<Block> owner, RegistryManager stage) {
        owner.getSlaveMap(BLOCKSTATE_TO_ID, ClearableObjectIntIdentityMap.class).clear();
    }

    public void onCreate(IForgeRegistryInternal<Block> owner, RegistryManager stage) {
        owner.setSlaveMap(BLOCKSTATE_TO_ID, STATE_ID = new ClearableObjectIntIdentityMap<>());
        owner.setSlaveMap(BLOCK_TO_ITEM, HashBiMap.create());
    }

    public Block createDummy(ResourceLocation key) {
        return new BlockDummyAir().setTranslationKey("air").setRegistryName(key);
    }

    public static final class BlockDummyAir extends BlockAir {
        private BlockDummyAir() {
        }
    }
}
