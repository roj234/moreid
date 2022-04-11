package moreid;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;

import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

/**
 * @author Roj234
 * @since  2020/10/4 23:09
 */
public class ErrorHandle {
    public static void throwException(String func, String mod) {
        try {
            throw new CustomModLoadingErrorDisplayException() {
                @Override
                public void initGui(GuiErrorScreen screen, FontRenderer renderer) {}

                @Override
                public void drawScreen(GuiErrorScreen gui, FontRenderer fr, int i, int i1, float v) {
                    fr.drawStringWithShadow("MoreId与" + mod + "存在已知的不兼容问题, 修改它的 " + func + " Mixin以解决问题", 20, gui.height / 2f, 0xff0000);
                }
            };
        } catch (Error e) {
            throw new RuntimeException("MoreId与" + mod + "存在已知的不兼容问题, 修改它的 " + func + " Mixin以解决问题");
        }
    }
}
