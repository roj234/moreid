package moreid.asm;

import moreid.MoreId;
import roj.asm.nixim.Inject;
import roj.asm.nixim.Nixim;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;

/**
 * @author Roj234
 */
@Nixim("net.minecraft.world.storage.WorldInfo")
//!!AT ["net.minecraft.world.storage.WorldInfo", ["func_76064_a"], true]
class NxSaverVer extends WorldInfo {
    @Inject("func_76064_a")
    public void updateTagCompound(NBTTagCompound nbt, NBTTagCompound playerNbt) {
        super.updateTagCompound(nbt, playerNbt);
        NBTTagCompound ver = nbt.getCompoundTag("Version");
        ver.setString("Name", "MoreId-" + MoreId.VERSION);
        ver.setInteger("Id", 9999);
    }
}
