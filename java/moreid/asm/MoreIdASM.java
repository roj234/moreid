package moreid.asm;

import ilib.asm.Loader;
import ilib.asm.NiximProxy;
import ilib.asm.Preloader;
import moreid.Config;
import roj.asm.AccessTransformer;
import roj.io.IOUtil;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Roj234
 */
@Name("MoreIdASM")
@MCVersion("1.12.2")
@TransformerExclusions("moreid.")
public class MoreIdASM implements IFMLLoadingPlugin {
    public static boolean fixedSurge, inited;

    static void init() {
        ilib.Config.replaceOIM = true;

        Class<MoreIdASM> me = MoreIdASM.class;
        AccessTransformer.readAndParseAt(me, "META-INF/moreid_at.cfg");

        try {
            if(Config.useNewChunkFormat) {
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxSaverVer.class"));
            }

            NiximProxy.Nx(IOUtil.read(me, "moreid/asm/Nx_MIChunkLoader.class"));
            NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxGameData.class"));
            NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxChunkPrimer.class"));
            Boolean side = Loader.testClientSide();
            if (Boolean.TRUE != side) {
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxBlockPalette.class"));
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxBlockPalette2.class"));
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxBlockPalette3.class"));
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxBlockPalette4.class"));
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxBlockPalette5.class"));
            }
            if (Boolean.FALSE != side) {
                NiximProxy.Nx(IOUtil.read(me, "moreid/asm/NxLoadSave.class"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Loader.addTransformer(new Transformer());

        Preloader.registerFileProcessor("mixins.surge.json", (input) -> {
            if(input.wIndex() == 681 && input.list[351] == 'G') {
                fixedSurge = true;
                input.setArray("{\"package\":\"net.darkhax.surge.mixins\",\"refmap\":\"mixins.surge.refmap.json\",\"target\":\"@env(DEFAULT)\",\"minVersion\":\"0.6\",\"compatibilityLevel\":\"JAVA_8\",\"mixins\":[\"minecraft.entity.passive.MixinEntitySheep\",\"minecraft.entity.MixinEntity\",\"minecraft.entity.item.MixinEntityItem\"],\"client\":[\"minecraftforge.client.model.animation.MixinModelBlockAnimation\",\"minecraft.client.resources.MixinFallbackResourceManager\",\"minecraft.client.resources.MixinSimpleReloadableResourceManager\",\"minecraft.client.gui.MixinGuiRepair\",\"minecraft.client.audio.MixinSoundHandler\"]}".getBytes(StandardCharsets.UTF_8));
                input.setArray("{\"package\":\"net.darkhax.surge.mixins\",\"refmap\":\"mixins.surge.refmap.json\",\"target\":\"@env(DEFAULT)\",\"minVersion\":\"0.6\",\"compatibilityLevel\":\"JAVA_8\",\"mixins\":[\"minecraft.entity.passive.MixinEntitySheep\",\"minecraft.entity.MixinEntity\",\"minecraft.entity.item.MixinEntityItem\"],\"client\":[\"minecraftforge.client.model.animation.MixinModelBlockAnimation\",\"minecraft.client.resources.MixinFallbackResourceManager\",\"minecraft.client.resources.MixinSimpleReloadableResourceManager\",\"minecraft.client.gui.MixinGuiRepair\",\"minecraft.client.audio.MixinSoundHandler\"]}".getBytes(StandardCharsets.UTF_8));
                return true;
            }
            if(input.lastIndexOf(new byte[] { 'G', 'a', 'm', 'e', 'D', 'a', 't', 'a'}) == -1) {
                fixedSurge = true;
            }
            return false;
        });
    }

    @Override
    public String[] getASMTransformerClass() {
        if (!inited) {
            init();
            inited = true;
        }
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
