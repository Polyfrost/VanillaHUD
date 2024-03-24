package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.gui.elements.ModCard;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.utils.InputHandler;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.HudPage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = ModsPage.class, remap = false)
public abstract class ModsPageMixin {

    @Shadow @Final private ArrayList<ModCard> modCards;

    @Inject(method = "reloadMods", at = @At(value = "TAIL"))
    private void remove(CallbackInfo ci) {
        ModsPage page = (ModsPage) ((Object) this);
        List<ModCard> cards = new ArrayList<>(modCards);
        cards.removeIf(card -> VanillaHUD.mods.contains(card.getModData()));
        if (page instanceof HudPage) {
            modCards.removeAll(cards);
        } else {
            modCards.clear();
            modCards.addAll(cards);
        }
    }

}
