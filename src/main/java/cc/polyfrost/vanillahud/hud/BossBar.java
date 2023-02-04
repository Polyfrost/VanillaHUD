package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.vanillahud.VanillaHUD;
import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.hud.SingleTextHud;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.BossStatus;
import org.lwjgl.opengl.GL11;

public class BossBar extends Config {

    @HUD(
            name = "Boss Bar"
    )
    public BossBarHUD hud = new BossBarHUD();

    public BossBar() {
        super(new Mod("Boss Bar", ModType.HUD), "bossbar.json");
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
                name = "Text Position",
                min = 0,
                max = 100
        )
        public float textPosition = 50;

        /** Gets OneConfig's Universal Minecraft instance. */
        @Exclude
        public final Minecraft mc = UMinecraft.getMinecraft();

        /** Gets OneConfig's Universal Minecraft fontRenderer. */
        @Exclude public final FontRenderer fontRenderer = UMinecraft.getFontRenderer();

        /**
         * The Boss Bar width
         */
        @Exclude public final int BAR_WIDTH = 182;

        @Exclude public float width = 0f;

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
            this.render(this.getText(example), this.isBossActive() ? BossStatus.healthScale : 0.8f, 0, this.renderText ? 10 : 0);
            UGraphics.GL.popMatrix();
            if (this.renderText) {
                super.draw(matrices, x + this.getWidth(1.0f, example) / 2 - (float) (this.fontRenderer.getStringWidth(this.getCompleteText(this.getText(example))) / 2), y, scale, example);
            }
        }

        @Override
        protected boolean shouldShow() {
            return this.isBossActive() && super.shouldShow();
        }

        private boolean isBossActive() {
            return BossStatus.bossName != null && BossStatus.statusBarTime > 0;
        }

        public void render(String bossName, float healthScale, float x, float y) {
            if (this.isBossActive()) {
                --BossStatus.statusBarTime;
            }

            UGraphics.enableBlend();
            UGraphics.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(Gui.icons);

            x = !this.renderText || fontRenderer.getStringWidth(bossName) < this.BAR_WIDTH ? x : x + (fontRenderer.getStringWidth(bossName) - this.BAR_WIDTH) * this.textPosition / 100.0F;

            float remainingHealth = healthScale * this.BAR_WIDTH;
            if (this.renderHealth) {
                this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, this.BAR_WIDTH, 5);
                this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 74, this.BAR_WIDTH, 5);
                if (remainingHealth > 0) {
                    this.mc.ingameGUI.drawTexturedModalRect(x, y, 0, 79, (int) remainingHealth, 5);
                }
            }

            this.width = this.renderHealth ? this.renderText ? Math.max(this.fontRenderer.getStringWidth(bossName), this.BAR_WIDTH) : this.BAR_WIDTH : (float) (this.fontRenderer.getStringWidth(bossName));
            UGraphics.disableBlend();
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return this.width * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            float barHeight = this.renderHealth ? 5.0F : 0.0F;
            float textHeight = this.renderText ? 9.0F : 0.0F;
            float extraHeight = this.renderText && this.renderHealth ? 1.0F : 0.0F;
            return (barHeight + textHeight + extraHeight) * scale;
        }
    }
}

