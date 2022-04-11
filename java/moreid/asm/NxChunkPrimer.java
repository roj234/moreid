package moreid.asm;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Inject;
import roj.asm.nixim.Inject.At;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

/**
 * @author Roj234
 * @since  2020/8/17 18:06
 */
@Nixim("net.minecraft.world.chunk.ChunkPrimer")
class NxChunkPrimer extends ChunkPrimer {
    @Shadow("field_177859_b")
    private static IBlockState DEFAULT_STATE;
    @Copy
    private final IBlockState[] field_177860_a; // char -> int 那么还有什么区别？？？

    @Inject(value = "<init>", at = At.REPLACE)
    public NxChunkPrimer() {
        this.field_177860_a = new IBlockState[65536];
    }

    @Inject("func_177856_a")
    public IBlockState getBlockState(int x, int y, int z) {
        IBlockState state = this.field_177860_a[getBlockIndex(x, y, z)];
        return state == null ? DEFAULT_STATE : state;
    }

    @Inject("func_177855_a")
    public void setBlockState(int x, int y, int z, IBlockState state) {
        this.field_177860_a[getBlockIndex(x, y, z)] = state;
    }

    @Shadow("func_186137_b")
    private static int getBlockIndex(int x, int y, int z) {
        return 0;
    }

    @Inject("func_186138_a")
    public int findGroundBlockIdx(int x, int z) { // todo test
        int index = (x << 12 | z << 8)/* + 256 - 1*/;

        for(int i = 255; i >= 0; --i) {
            IBlockState state = this.field_177860_a[index + i];
            if (state != null && state != DEFAULT_STATE) {
                return i;
            }
        }

        return 0;
    }
}
