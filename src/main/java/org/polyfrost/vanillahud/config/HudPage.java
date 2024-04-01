package org.polyfrost.vanillahud.config;

import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.ModCard;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.font.Fonts;
import cc.polyfrost.oneconfig.utils.InputHandler;
import cc.polyfrost.oneconfig.utils.SearchUtils;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.mixin.ModsPageAccessor;

import java.util.ArrayList;

public class HudPage extends ModsPage {

    @Override
    public int drawStatic(long vg, int x, int y, InputHandler inputHandler) {
        return 0;
    }

    @Override
    public void draw(long vg, int x, int y, InputHandler inputHandler) {
        ModsPageAccessor accessor = (ModsPageAccessor) ModConfig.page;
        String filter = OneConfigGui.INSTANCE == null ? "" : OneConfigGui.INSTANCE.getSearchValue().toLowerCase().trim();
        int iX = x + 16;
        int iY = y + 16;
        ArrayList<ModCard> finalModCards = new ArrayList<>(accessor.getModCards());
        for (ModCard modCard : finalModCards) {
            if (filter.equals("") || SearchUtils.isSimilar(modCard.getModData().name, filter)) {
                if (iY + 135 >= y - scroll && iY <= y + 728 - scroll) modCard.draw(vg, iX, iY, inputHandler);
                iX += 260;
                if (iX > x + 796) {
                    iX = x + 16;
                    iY += 135;
                }
            }
        }
        accessor.setSize(iY - y + 135);
    }

    @Override
    public String getTitle() {
        return VanillaHUD.NAME;
    }

    @Override
    public boolean isBase() {
        return false;
    }
}