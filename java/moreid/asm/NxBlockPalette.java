package moreid.asm;

import io.netty.handler.codec.DecoderException;
import roj.asm.nixim.Copy;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.IBlockStatePalette;

/**
 * @author Roj233
 * @since 2022/4/20 14:06
 */
@Nixim("net.minecraft.world.chunk.BlockStateContainer")
class NxBlockPalette extends BlockStateContainer {
    @Shadow("field_186023_d")
    private static IBlockStatePalette REGISTRY_BASED_PALETTE;
    @Shadow("field_186024_e")
    int bits;
    @Shadow("func_186012_b")
    private void setBits(int i) {}

    @Copy
    public void func_186010_a(PacketBuffer buf) {
        this.setBits(buf.readByte());

        this.palette.read(buf);
        long[] arr = storage.getBackingLongArray();
        if (buf.readVarInt() != arr.length) throw new DecoderException("Palette size mismatch");
        for(int j = 0; j < arr.length; ++j) {
            arr[j] = buf.readLong();
        }

        int regSize = MathHelper.log2DeBruijn(Block.BLOCK_STATE_IDS.size());
        if (this.palette == REGISTRY_BASED_PALETTE && this.bits != regSize) {
            this.onResize(regSize, AIR_BLOCK_STATE);
        }
    }
}
