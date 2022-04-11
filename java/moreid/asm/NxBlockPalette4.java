package moreid.asm;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.BlockStatePaletteLinear;

/**
 * @author Roj233
 * @since 2022/4/20 14:06
 */
@Nixim("net.minecraft.world.chunk.BlockStatePaletteLinear")
class NxBlockPalette4 extends BlockStatePaletteLinear {
    @Shadow("field_186045_d")
    private int arraySize;
    @Shadow("field_186042_a")
    private IBlockState[] states;

    NxBlockPalette4() {
        super(0, null);
    }

    @Copy
    public void func_186038_a(PacketBuffer buf) {
        this.arraySize = buf.readVarInt();

        for(int i = 0; i < this.arraySize; ++i) {
            this.states[i] = Block.BLOCK_STATE_IDS.getByValue(buf.readVarInt());
        }
    }
}
