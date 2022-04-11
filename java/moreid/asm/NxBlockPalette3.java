package moreid.asm;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.world.chunk.BlockStatePaletteHashMap;

/**
 * @author Roj233
 * @since 2022/4/20 14:06
 */
@Nixim("net.minecraft.world.chunk.BlockStatePaletteHashMap")
class NxBlockPalette3 extends BlockStatePaletteHashMap {
    @Shadow("field_186046_a")
    private IntIdentityHashBiMap<IBlockState> statePaletteMap;

    NxBlockPalette3() {
        super(0, null);
    }

    @Copy
    public void func_186038_a(PacketBuffer buf) {
        this.statePaletteMap.clear();
        int size = buf.readVarInt();

        for(int i = 0; i < size; ++i) {
            this.statePaletteMap.add(Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt()));
        }
    }
}
