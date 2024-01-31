package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.config.elements.OptionPage;
import cc.polyfrost.oneconfig.hud.HUDUtils;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import org.polyfrost.vanillahud.hud.TabList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(value = HUDUtils.class, remap = false)
public class HUDUtilsMixin {

    @Inject(method = "addHudOptions", at = @At("TAIL"))
    private static void hudUtils$modifyOptions(OptionPage page, Field field, Object instance, Config config, CallbackInfo ci) {
        if (!(ConfigUtils.getField(field, instance) instanceof TabList.TabHud)) return;
        for (BasicOption option : HudCore.hudOptions) {
            String fieldName = option.getField().getName();
            if (fieldName.contains("pingLevel")) fieldName = "pingLevel";
            Object hud = option.getParent();
            boolean isTabList = hud instanceof TabList.TabHud;
            if (!isTabList) continue;
            switch (fieldName) {
                case "pingType":
                case "hideFalsePing":
                case "pingLevel":
                    option.addDependency("showPing", () -> TabList.TabHud.showPing);
                    option.addDependency("numberPing", () -> TabList.TabHud.numberPing);
                case "numberPing":
                    option.addDependency("showPing", () -> TabList.TabHud.showPing);
            }
        }
    }

}
