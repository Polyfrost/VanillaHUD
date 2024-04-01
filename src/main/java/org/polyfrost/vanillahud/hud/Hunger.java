package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.polyfrost.vanillahud.config.HudConfig;

import static org.polyfrost.vanillahud.hud.BossBar.BossBarHUD.mc;

public class Hunger extends HudConfig {

    @HUD(
            name = "Hunger"
    )
    public static HungerHud hud = new HungerHud();

    @DualOption(
            name = "Mode",
            left = "Down",
            right = "Up",
            category = "Mount Health"
    )
    public static boolean mode = true;

    @HUD(
            name = "Mount Health",
            category = "Mount Health"
    )
    public static MountHud mountHud = new MountHud();

    public static HudBar getMountHud() {
        return Hunger.mountHud.isEnabled() ? Hunger.mountHud : Hunger.hud;
    }

    public Hunger() {
        super(new Mod("Hunger", ModType.HUD), "vanilla-hud/hunger.json");
        initialize();
    }

    public static class HungerHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public HungerHud() {
            super(true, 1920 / 2f + 182 / 2f - 81, 1080 - 39, true);
        }
    }

    public static class MountHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        public MountHud() {
            super(false, 1920 / 2f + 182 / 2f - 81, 1080 - 59, true);
        }
    }

    public static int mountLink() {
        if (HudCore.editing) return 0;
        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        Entity tmp = player.ridingEntity;
        if (!(tmp instanceof EntityLivingBase)) return 0;
        EntityLivingBase mount = (EntityLivingBase) tmp;
        int hearts = (int) (mount.getMaxHealth() + 0.5F) / 2;
        if (hearts > 30) hearts = 30;
        int rows = MathHelper.ceiling_float_int(hearts / 10f) - 1;
        HudBar hud = getMountHud();
        return  (int) (rows * 10 * (Hunger.mode ? 1 : -1) * hud.getScale());
    }
}
