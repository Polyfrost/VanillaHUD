package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Checkbox;
import cc.polyfrost.oneconfig.config.annotations.DualOption;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import static org.polyfrost.vanillahud.hud.BossBar.BossBarHUD.mc;

public class Hunger extends Config {

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
            super(true);
        }
    }

    public static class MountHud extends HudBar {

        @Checkbox(name = "Link with health")
        public static boolean healthLink = false;

        public MountHud() {
            super(false);
        }
    }

    public static int mountLink() {
        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        Entity tmp = player.ridingEntity;
        if (!(tmp instanceof EntityLivingBase)) return 0;
        EntityLivingBase mount = (EntityLivingBase) tmp;
        int hearts = (int) (mount.getMaxHealth() + 0.5F) / 2;
        if (hearts > 30) hearts = 30;
        int rows = MathHelper.ceiling_float_int(hearts / 10f) - 1;
        HudBar hud = getMountHud();
        return HudCore.editing ? 0 : (int) (rows * 10 * (Hunger.mode ? 1 : -1) * hud.getScale());
    }
}
