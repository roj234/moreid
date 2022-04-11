package moreid.asm;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Nixim;

import net.minecraft.network.PacketBuffer;

/**
 * @author Roj233
 * @since 2022/4/20 14:06
 */
@Nixim("net.minecraft.world.chunk.IBlockStatePalette")
interface NxBlockPalette2 {
    @Copy
    void func_186038_a(PacketBuffer buf);
}
