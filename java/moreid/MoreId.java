package moreid;

import ilib.command.MasterCommand;
import ilib.command.sub.MySubs;
import ilib.util.PlayerUtil;
import moreid.asm.MoreIdASM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
        modid = "moreid",
        name = "MoreId",
        version = MoreId.VERSION,
        acceptedMinecraftVersions = "[1.12, 1.13)",
        dependencies = "required:forge@[14.23.4.2768,); required:ilib@[0.4.0,)"
)
public class MoreId {
    public static final String VERSION = "2.2.0";

    public static final Logger logger = LogManager.getLogger("MoreId");

    public static Logger logger() { return logger; }

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if(Loader.isModLoaded("surge") && !MoreIdASM.fixedSurge) {
            ErrorHandle.throwException("GameData", "Surge");
        }
    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        if (Loader.isModLoaded("crafttweaker")) {
            event.registerServerCommand(new MasterCommand("moreid", 3).register(new MySubs("ctrl", "重载合成表") {
                public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
                    PlayerUtil.sendTo(sender, "重载中.");
                    try {
                        CTRL.refreshRecipe();
                        PlayerUtil.sendTo(sender, "\u00a7b已重载.");
                    } catch (Throwable e) {
                        PlayerUtil.sendTo(sender, "\u00a7c重载失败.");
                    }
                }
            }));
        }
    }
}
