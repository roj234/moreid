package moreid.asm;

import roj.asm.nixim.Copy;
import roj.asm.nixim.Nixim;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.BlockStatePaletteRegistry;

/**
 * @author Roj233
 * @since 2022/4/20 14:06
 */
@Nixim("net.minecraft.world.chunk.BlockStatePaletteRegistry")
class NxBlockPalette5 extends BlockStatePaletteRegistry {
    NxBlockPalette5() {}

    @Copy
    public void func_186038_a(PacketBuffer buf) {
        buf.readVarInt();
    }
}
