package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.*;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.entity.boss.BossStatus;

public class BossBar extends Config {

    @HUD(
            name = "Boss Bar"
    )
    public BossBarHUD hud = new BossBarHUD();

    public BossBar() {
        super(new Mod("Boss Bar", ModType.HUD, "/vanillahud_dark.svg"), "bossbar.json");
        initialize();
    }

    public static class BossBarHUD extends SingleTextHud {

        @Switch(
                name = "Render Text"
        )
        public boolean renderText = true;

        @Switch(
                name = "Render Health"
        )
        public boolean renderHealth = true;

        @Slider(
                name = "Bar Position",
                min = 0,
                max = 100
        )
        public float barPosition = 50;

        /** Gets OneConfig's Universal Minecraft instance. */
        @Exclude
        public static final Minecraft mc = UMinecraft.getMinecraft();

        /** Gets OneConfig's Universal Minecraft fontRenderer. */
        @Exclude public static final FontRenderer fontRenderer = UMinecraft.getFontRenderer();

        /**
         * The Boss Bar width
         */
        @Exclude public static final int BAR_WIDTH = 182;

        public BossBarHUD() {
            super("", true, 1920f / 2, 2f, 1, false, false, 0, 0, 0, new OneColor(0, 0, 0, 120), false, 2, new OneColor(0, 0, 0));
            this.textType = 1;
            EventManager.INSTANCE.register(this);
        }

        @Override
        protected String getText(boolean example) {
            return this.getTextFrequent(example);
        }

        protected String getTextFrequent(boolean example) {
            return this.isBossActive() ? BossStatus.bossName : "Wither";
        }

        @Override
        public void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            UGraphics.GL.pushMatrix();
            UGraphics.GL.scale(scale, scale, 1);
            UGraphics.GL.translate(x / scale, y / scale, 1);
            this.drawHealth(this.getCompleteText(this.getText(example)), this.isBossActive() ? BossStatus.healthScale : 0.8f, 0, this.renderText ? 10 : 0);
            UGraphics.GL.popMatrix();
            if (this.renderText) {
                super.draw(matrices, x + this.getWidth(1.0f, example) / 2 - (float) (fontRenderer.getStringWidth(this.getCompleteText(this.getText(example))) / 2), y, scale, example);
            }
        }

        @Override
        protected boolean shouldShow() {
            return this.isBossActive() && super.shouldShow();
        }

        private boolean isBossActive() {
            return BossStatus.bossName != null && BossStatus.statusBarTime > 0;
        }

        public void drawHealth(String bossName, float healthScale, float x, float y) {
            if (this.isBossActive()) {
                --BossStatus.statusBarTime;
            }

            UGraphics.enableBlend();
            UGraphics.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(Gui.icons);

            if (this.renderText && fontRenderer.getStringWidth(bossName) > BAR_WIDTH) {
                x += (fontRenderer.getStringWidth(bossName) - BAR_WIDTH) * this.barPosition / 100.0F;
            }

            float remainingHealth = healthScale * BAR_WIDTH;
            if (this.renderHealth) {
                mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, BAR_WIDTH, 5);
                mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, BAR_WIDTH, 5);
                if (remainingHealth > 0) {
                    mc.ingameGUI.drawTexturedModalRect(x, y, 0, 79, (int) remainingHealth, 5);
                }
            }

            UGraphics.disableBlend();
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            float textWidth = this.renderText ? UMinecraft.getFontRenderer().getStringWidth(this.getCompleteText(getText(example))) : 0.0f;
            float healthWidth = this.renderHealth ? this.BAR_WIDTH : 0.0f;
            return Math.max(textWidth, healthWidth) * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            float height = 0.0f;

            if (this.renderHealth) {
                height += 5.0F;
            }

            if (this.renderText) {
                height += 9.0F;
            }

            if (this.renderText && this.renderHealth) {
                height += 1.0F;
            }

            return height * scale;
        }
    }
}

