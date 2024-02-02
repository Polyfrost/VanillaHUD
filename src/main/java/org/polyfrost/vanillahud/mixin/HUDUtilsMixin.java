package org.polyfrost.vanillahud.mixin;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.elements.*;
import cc.polyfrost.oneconfig.hud.HUDUtils;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import org.polyfrost.vanillahud.hud.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(value = HUDUtils.class, remap = false)
public class HUDUtilsMixin {

    @Inject(method = "addHudOptions", at = @At("TAIL"))
    private static void hudUtils$modifyOptions(OptionPage page, Field field, Object instance, Config config, CallbackInfo ci) {
        Hud hud = (Hud) ConfigUtils.getField(field, instance);
        if (!(hud instanceof TabList.TabHud) && !(hud instanceof Scoreboard.ScoreboardHUD)) return;
        HudCore.hudOptions.removeIf(HUDUtilsMixin::hudUtils$addDependency);
        HUD hudAnnotation = field.getAnnotation(HUD.class);
        ConfigUtils.getSubCategory(page, hudAnnotation.category(), hudAnnotation.subcategory()).options.removeIf(HUDUtilsMixin::hudUtils$addDependency);
    }

    private static boolean hudUtils$addDependency(BasicOption option) {
        String fieldName = option.getField().getName();
        if (fieldName.contains("pingLevel")) fieldName = "pingLevel";
        Object hud = option.getParent();
        boolean isTabList = hud instanceof TabList.TabHud;
        boolean isScoreboard = hud instanceof Scoreboard.ScoreboardHUD;
        if (!isTabList && !isScoreboard) return false;
        switch (fieldName) {
            case "pingType":
            case "hideFalsePing":
            case "pingLevel":
                option.addDependency("showPing", () -> TabList.TabHud.showPing);
                option.addDependency("numberPing", () -> TabList.TabHud.numberPing);
                break;
            case "numberPing":
                option.addDependency("showPing", () -> TabList.TabHud.showPing);
            case "scorePointsColor":
                option.addDependency("scoreboardPoints", () -> Scoreboard.ScoreboardHUD.scoreboardPoints);
                break;
            case "persistentTitle":
            case "titleColor":
                option.addDependency("scoreboardTitle", () -> Scoreboard.ScoreboardHUD.scoreboardTitle);
        }
        return false;
    }

}
