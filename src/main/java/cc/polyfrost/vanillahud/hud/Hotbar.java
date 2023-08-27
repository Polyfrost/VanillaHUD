package cc.polyfrost.vanillahud.hud;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Exclude;
import cc.polyfrost.oneconfig.config.annotations.HUD;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.hud.Hud;
import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.libs.universal.UResolution;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import cc.polyfrost.vanillahud.mixin.GuiAccessor;
import cc.polyfrost.vanillahud.mixin.GuiIngameAccessor;
import cc.polyfrost.vanillahud.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class Hotbar extends Config {
    @HUD(
            name = "Hotbar"
    )
    public HotbarHUD hud = new HotbarHUD();

    @HUD(
            name = "Heart / Armor Status"
    )
    public HeartStatusHUD heartStatus = new HeartStatusHUD();

    public Hotbar() {
        super(new Mod("Hotbar", ModType.HUD, "/vanillahud_dark.svg"), "hotbar.json");
        initialize();
    }

    /** Gets OneConfig's Universal Minecraft instance. */
    @Exclude
    private static final Minecraft mc = UMinecraft.getMinecraft();

    public static class HotbarHUD extends Hud {

        @Switch(
                name = "Render Stack Size"
        )
        public boolean renderStackSize = true;

        @Dropdown(
                name = "Text Type",
                options = {"No Shadow", "Shadow", "Full Shadow"}
        )
        public int textType = 1;

        @Exclude
        protected static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png");

        public HotbarHUD() {
            super(true, 1920 / 2f - 182 / 2f, 1080 - 22);
            showInDebug = true;
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            mc.getTextureManager().bindTexture(widgetsTexPath);
            EntityPlayer entityPlayer = (EntityPlayer) mc.getRenderViewEntity();
            GuiAccessor zLevelAccessor = (GuiAccessor) mc.ingameGUI;
            float f = zLevelAccessor.getZLevel();
            zLevelAccessor.setZLevel(-90.0f);
            UGraphics.GL.pushMatrix();
            UGraphics.GL.scale(scale, scale, 1);
            UGraphics.GL.translate(x / scale, y / scale, 0f);
            mc.ingameGUI.drawTexturedModalRect(0, 0, 0, 0, 182, 22);
            mc.ingameGUI.drawTexturedModalRect(-1 + entityPlayer.inventory.currentItem * 20, -1, 0, 22, 24, 22);
            zLevelAccessor.setZLevel(f);
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            for (int j = 0; j < 9; ++j) {
                int k = 1 + j * 20 + 2;
                int l = 3;
                renderHotbarItem(j, k, l, entityPlayer);
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            UGraphics.GL.popMatrix();
        }

        protected void renderHotbarItem(int index, float xPos, float yPos, EntityPlayer player) {
            ItemStack itemStack = player.inventory.mainInventory[index];
            if (itemStack == null) {
                return;
            }
            float f = (float)itemStack.animationsToGo - ((MinecraftAccessor)UMinecraft.getMinecraft()).getTimer().renderPartialTicks;
            if (f > 0.0f) {
                GlStateManager.pushMatrix();
                float g = 1.0f + f / 5.0f;
                GlStateManager.translate(xPos + 8, yPos + 12, 0.0f);
                GlStateManager.scale(1.0f / g, (g + 1.0f) / 2.0f, 1.0f);
                GlStateManager.translate(-(xPos + 8), -(yPos + 12), 0.0f);
            }
            renderItemAndEffectIntoGUI(itemStack, xPos, yPos);
            if (f > 0.0f) {
                GlStateManager.popMatrix();
            }
            renderItemOverlayIntoGUI(mc.fontRendererObj, itemStack, xPos, yPos);
        }

        public void renderItemAndEffectIntoGUI(final ItemStack stack, float xPosition, float yPosition) {
            if (stack != null && stack.getItem() != null) {
                try {
                    IBakedModel ibakedmodel = mc.getRenderItem().getItemModelMesher().getItemModel(stack);
                    GlStateManager.pushMatrix();
                    mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                    mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.enableAlpha();
                    GlStateManager.alphaFunc(516, 0.1f);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

                    GlStateManager.translate(xPosition, yPosition, 150f);
                    GlStateManager.translate(8.0f, 8.0f, 0.0f);
                    GlStateManager.scale(1.0f, 1.0f, -1.0f);
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                    if (ibakedmodel.isGui3d()) {
                        GlStateManager.scale(40.0f, 40.0f, 40.0f);
                        GlStateManager.rotate(210.0f, 1.0f, 0.0f, 0.0f);
                        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
                        GlStateManager.enableLighting();
                    } else {
                        GlStateManager.scale(64.0f, 64.0f, 64.0f);
                        GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
                        GlStateManager.disableLighting();
                    }

                    ibakedmodel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GUI);
                    mc.getRenderItem().renderItem(stack, ibakedmodel);
                    GlStateManager.disableAlpha();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.disableLighting();
                    GlStateManager.popMatrix();
                    mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                    mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
                }
                catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering item");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being rendered");
                    crashreportcategory.addCrashSectionCallable("Item Type", () -> String.valueOf(stack.getItem()));
                    crashreportcategory.addCrashSectionCallable("Item Aux", () -> String.valueOf(stack.getMetadata()));
                    crashreportcategory.addCrashSectionCallable("Item NBT", () -> String.valueOf(stack.getTagCompound()));
                    crashreportcategory.addCrashSectionCallable("Item Foil", () -> String.valueOf(stack.hasEffect()));
                    throw new ReportedException(crashreport);
                }
            }
        }

        private void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, float xPosition, float yPosition) {
            if (stack != null) {
                if (stack.stackSize != 1 && renderStackSize) {
                    String s;
                    s = String.valueOf(stack.stackSize);
                    if (stack.stackSize < 1) {
                        s = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
                    }
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.disableBlend();
                    switch (textType) {
                        case 0:
                            Platform.getGLPlatform().drawText(s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 0xFFFFFF, false);
                            break;
                        case 1:
                            Platform.getGLPlatform().drawText(s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 0xFFFFFF, true);
                            break;
                        case 2:
                            TextRenderer.drawBorderedText(s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 0xFFFFFF, 100);
                            break;
                    }
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
                if (stack.getItem().showDurabilityBar(stack)) {
                    double health = stack.getItem().getDurabilityForDisplay(stack);
                    int j = (int)Math.round(13.0 - health * 13.0);
                    int i = (int)Math.round(255.0 - health * 255.0);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                    draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
                    draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);
                    GlStateManager.enableAlpha();
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }

        private void draw(WorldRenderer renderer, float x, float y, int width, int height, int red, int green, int blue, int alpha) {
            renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            renderer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
            renderer.pos(x, y + height, 0.0).color(red, green, blue, alpha).endVertex();
            renderer.pos(x + width, y + height, 0.0).color(red, green, blue, alpha).endVertex();
            renderer.pos(x + width, y, 0.0).color(red, green, blue, alpha).endVertex();
            Tessellator.getInstance().draw();
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return 182 * scale;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return 22 * scale;
        }

        @Override
        protected boolean shouldShow() {
            return super.shouldShow() && mc.getRenderViewEntity() instanceof EntityPlayer;
        }
    }

    public static class HeartStatusHUD extends BasicHud {
        @Exclude
        public static boolean renderHealthBar;
        @Exclude
        public static boolean renderArmorBar;
        @Exclude
        protected int playerHealth = 0;
        @Exclude
        protected int lastPlayerHealth = 0;
        /**
         * The last recorded system time
         */
        @Exclude
        protected long lastSystemTime = 0L;
        /**
         * Used with updateCounter to make the heart bar flash
         */
        @Exclude
        protected long healthUpdateCounter = 0L;
        @Exclude
        private final Random rand = new Random();

        public HeartStatusHUD() {
            super(true, UResolution.getScaledWidth() / 2f - 91, UResolution.getScaledHeight() - 39, 1);
        }

        @Override
        protected void draw(UMatrixStack matrices, float x, float y, float scale, boolean example) {
            mc.getTextureManager().bindTexture(Gui.icons);
            if (renderHealthBar) {
                GlStateManager.enableBlend();

                EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
                int health = MathHelper.ceiling_float_int(player.getHealth());
                int updateCounter = ((GuiIngameAccessor) mc.ingameGUI).getUpdateCounter();
                boolean highlight = healthUpdateCounter > (long)updateCounter && (healthUpdateCounter - (long)updateCounter) / 3L %2L == 1L;

                if (health < this.playerHealth && player.hurtResistantTime > 0)
                {
                    this.lastSystemTime = Minecraft.getSystemTime();
                    this.healthUpdateCounter = updateCounter + 20;
                }
                else if (health > this.playerHealth && player.hurtResistantTime > 0)
                {
                    this.lastSystemTime = Minecraft.getSystemTime();
                    this.healthUpdateCounter = updateCounter + 10;
                }

                if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L)
                {
                    this.playerHealth = health;
                    this.lastPlayerHealth = health;
                    this.lastSystemTime = Minecraft.getSystemTime();
                }

                this.playerHealth = health;
                int healthLast = this.lastPlayerHealth;

                IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
                float healthMax = (float)attrMaxHealth.getAttributeValue();
                float absorb = player.getAbsorptionAmount();

                int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
                int rowHeight = Math.max(10 - (healthRows - 2), 3);

                this.rand.setSeed(updateCounter * 312871L);

                GuiIngameForge.left_height += (healthRows * rowHeight);
                if (rowHeight != 10) GuiIngameForge.left_height += 10 - rowHeight;

                int regen = -1;
                if (player.isPotionActive(Potion.regeneration))
                {
                    regen = updateCounter % 25;
                }

                final int TOP =  9 * (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
                final int BACKGROUND = (highlight ? 25 : 16);
                int MARGIN = 16;
                if (player.isPotionActive(Potion.poison))      MARGIN += 36;
                else if (player.isPotionActive(Potion.wither)) MARGIN += 72;
                float absorbRemaining = absorb;

                for (int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
                {
                    //int b0 = (highlight ? 1 : 0);
                    int row = MathHelper.ceiling_float_int((float)(i + 1) / 10.0F) - 1;
                    float left = x + i % 10 * 8;
                    float top = y - row * rowHeight;

                    if (health <= 4) top += rand.nextInt(2);
                    if (i == regen) top -= 2;

                    mc.ingameGUI.drawTexturedModalRect(left, top, BACKGROUND, TOP, 9, 9);

                    if (highlight)
                    {
                        if (i * 2 + 1 < healthLast)
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 54, TOP, 9, 9); //6
                        else if (i * 2 + 1 == healthLast)
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 63, TOP, 9, 9); //7
                    }

                    if (absorbRemaining > 0.0F)
                    {
                        if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 153, TOP, 9, 9); //17
                        else
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 144, TOP, 9, 9); //16
                        absorbRemaining -= 2.0F;
                    }
                    else
                    {
                        if (i * 2 + 1 < health)
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 36, TOP, 9, 9); //4
                        else if (i * 2 + 1 == health)
                            mc.ingameGUI.drawTexturedModalRect(left, top, MARGIN + 45, TOP, 9, 9); //5
                    }
                }

                GlStateManager.disableBlend();
            }

            if (renderArmorBar) {
                GlStateManager.enableBlend();
                float left = x;
                float top = y - GuiIngameForge.left_height - 39;

                int level = ForgeHooks.getTotalArmorValue(mc.thePlayer);
                for (int i = 1; level > 0 && i < 20; i += 2)
                {
                    if (i < level)
                    {
                        mc.ingameGUI.drawTexturedModalRect(left, top, 34, 9, 9, 9);
                    }
                    else if (i == level)
                    {
                        mc.ingameGUI.drawTexturedModalRect(left, top, 25, 9, 9, 9);
                    }
                    else if (i > level)
                    {
                        mc.ingameGUI.drawTexturedModalRect(left, top, 16, 9, 9, 9);
                    }
                    left += 8;
                }
                GuiIngameForge.left_height += 10;

                GlStateManager.disableBlend();
            }
        }

        @Override
        protected float getWidth(float scale, boolean example) {
            return 200;
        }

        @Override
        protected float getHeight(float scale, boolean example) {
            return 200;
        }

        @Override
        protected boolean shouldShow() {
            return super.shouldShow() && mc.getRenderViewEntity() instanceof EntityPlayer;
        }
    }
}
