package org.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.internal.hud.HudCore;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.MathHelper;
import org.polyfrost.vanillahud.config.HudConfig;

import static org.polyfrost.vanillahud.hud.BossBar.BossBarHUD.mc;

public class Health extends HudConfig {

    @HUD(
            name = "Health"
    )
    public static HealthHud hud = new HealthHud();

    public Health() {
        super(new Mod("Health", ModType.HUD), "vanilla-hud/health.json");
        initialize();
    }

    public static class HealthHud extends HudBar {

        @DualOption(
                name = "Mode",
                left = "Down",
                right = "Up"
        )
        public static boolean mode = true;

        @Checkbox(name = "Link with mount health")
        public static boolean mountLink = false;

        public HealthHud() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 39, false);
        }
    }

    public static int healthLink() {
        IAttributeInstance attrMaxHealth = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        float healthMax = (float)attrMaxHealth.getAttributeValue();
        float absorb = mc.thePlayer.getAbsorptionAmount();
        int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        int height = healthRows * rowHeight;
        if (rowHeight != 10) height += 10 - rowHeight;
        return HudCore.editing ? 0 : (int) ((height - 10) * (Health.HealthHud.mode ? 1 : -1) * Health.hud.getScale());
    }
}
