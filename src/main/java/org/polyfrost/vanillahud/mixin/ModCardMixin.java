package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.gui.OneConfigGui;
import cc.polyfrost.oneconfig.gui.elements.ModCard;
import cc.polyfrost.oneconfig.gui.pages.ModsPage;
import org.polyfrost.vanillahud.VanillaHUD;
import org.polyfrost.vanillahud.config.HudPage;
import org.polyfrost.vanillahud.config.ModConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModCard.class, remap = false)
public class ModCardMixin {

    @Shadow @Final private Mod modData;

    @Inject(method = "onClick", at = @At(value = "INVOKE", target = "Lcc/polyfrost/oneconfig/gui/OneConfigGui;openPage(Lcc/polyfrost/oneconfig/gui/pages/Page;)V"), cancellable = true)
    private void page(CallbackInfo ci) {
        if (modData == VanillaHUD.modConfig.mod) {
            ModsPage page = new HudPage();
            ModConfig.page = page;
            OneConfigGui.INSTANCE.openPage(page);
            ci.cancel();
        }
    }
}
