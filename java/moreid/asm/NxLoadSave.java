package moreid.asm;

import roj.asm.nixim.Inject;
import roj.asm.nixim.Nixim;
import roj.asm.nixim.Shadow;

/**
 * @author Roj234
 */
@Nixim("net.minecraft.world.storage.WorldSummary")
class NxLoadSave {
    @Shadow("field_186359_j")
    int versionId;

    @Inject("func_186356_m")
    public boolean askToOpenWorld() {
        return versionId > 1343 && versionId != 9999;
    }
}
