package org.polyfrost.vanillahud.mixin;

//? if >=1.21.8 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
//? if <=1.21.8
/*import net.minecraft.client.Minecraft;*/
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GuiGraphicsExtractor.class, priority = 1500)
public abstract class DrawCaptureMixin {
    //? if >1.21.8 {
    @Shadow
    private static GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
        throw new AssertionError();
    }
    //?} else {
    /*@Unique
    private static GuiSpriteScaling getSpriteScaling(TextureAtlasSprite sprite) {
        return Minecraft.getInstance().getGuiSprites().getSpriteScaling(sprite);
    }
    *///?}

    @Unique
    private GuiGraphicsExtractor vanillahud$self() {
        return (GuiGraphicsExtractor) (Object) this;
    }

    @WrapMethod(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIII)V")
    private void vanillahud$sprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int w, int h, int color, Operation<Void> original) {
        boolean stretched = getSpriteScaling(sprite) instanceof GuiSpriteScaling.Stretch;
        if (pipeline == RenderPipelines.GUI_TEXTURED && stretched && HudCapture.sprite(vanillahud$self(), sprite, x, y, w, h, color)) return;
        original.call(pipeline, sprite, x, y, w, h, color);
    }

    @WrapMethod(method = "blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIIIIIII)V")
    private void vanillahud$spritePart(RenderPipeline pipeline, TextureAtlasSprite sprite, int texW, int texH, int u, int v, int x, int y, int w, int h, int color, Operation<Void> original) {
        if (pipeline == RenderPipelines.GUI_TEXTURED && HudCapture.sprite(vanillahud$self(), sprite, texW, texH, u, v, w, h, x, y, w, h, color)) return;
        original.call(pipeline, sprite, texW, texH, u, v, x, y, w, h, color);
    }

    @WrapMethod(method = "innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIFFFFI)V")
    private void vanillahud$texture(RenderPipeline pipeline, Identifier id, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, Operation<Void> original) {
        if (pipeline == RenderPipelines.GUI_TEXTURED && HudCapture.texture(vanillahud$self(), id, x0, x1, y0, y1, u0, u1, v0, v1, color)) return;
        original.call(pipeline, id, x0, x1, y0, y1, u0, u1, v0, v1, color);
    }

    @WrapMethod(method = "fill(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;IIIII)V")
    private void vanillahud$fill(RenderPipeline pipeline, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        if (pipeline == RenderPipelines.GUI && HudCapture.fill(vanillahud$self(), x0, y0, x1, y1, color)) return;
        original.call(pipeline, x0, y0, x1, y1, color);
    }

    @WrapMethod(method = "enableScissor")
    private void vanillahud$clipStart(int x0, int y0, int x1, int y1, Operation<Void> original) {
        HudCapture.clipStart(vanillahud$self(), x0, y0, x1, y1);
        original.call(x0, y0, x1, y1);
    }

    @WrapMethod(method = "disableScissor")
    private void vanillahud$clipEnd(Operation<Void> original) {
        HudCapture.clipEnd();
        original.call();
    }

    //? if >=26 {
    @WrapMethod(method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V")
    //?} else
    /*@WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V")*/
    private void vanillahud$item(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, Operation<Void> original) {
        if (HudCapture.item(vanillahud$self(), stack, x, y)) return;
        original.call(entity, level, stack, x, y, seed);
    }

    //? if >=26 {
    @WrapMethod(method = "text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V")
    //?} else
    /*@WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V")*/
    private void vanillahud$string(Font font, String text, int x, int y, int color, boolean shadow, Operation<Void> original) {
        if (text != null && HudCapture.text(vanillahud$self(), text, x, y, color, TextShadowHook.apply(shadow))) return;
        original.call(font, text, x, y, color, shadow);
    }

    //? if >=26 {
    @WrapMethod(method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V")
    //?} else
    /*@WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V")*/
    private void vanillahud$sequence(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, Operation<Void> original) {
        if (HudCapture.text(vanillahud$self(), text, x, y, color, TextShadowHook.apply(shadow))) return;
        original.call(font, text, x, y, color, shadow);
    }
}
//?} elif >=1.21.2 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(value = GuiGraphicsExtractor.class, priority = 1500)
public abstract class DrawCaptureMixin {
    @Unique
    private GuiGraphicsExtractor vanillahud$self() {
        return (GuiGraphicsExtractor) (Object) this;
    }

    @Unique
    private static boolean vanillahud$plain(Function<Identifier, RenderType> type, Identifier texture) {
        return type.apply(texture) == RenderType.guiTextured(texture);
    }

    @WrapMethod(method = "blitSprite(Ljava/util/function/Function;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIII)V")
    private void vanillahud$sprite(Function<Identifier, RenderType> type, TextureAtlasSprite sprite, int x, int y, int w, int h, int color, Operation<Void> original) {
        boolean stretched = Minecraft.getInstance().getGuiSprites().getSpriteScaling(sprite) instanceof GuiSpriteScaling.Stretch;
        if (stretched && vanillahud$plain(type, sprite.atlasLocation()) && HudCapture.sprite(vanillahud$self(), sprite, x, y, w, h, color)) return;
        original.call(type, sprite, x, y, w, h, color);
    }

    @WrapMethod(method = "blitSprite(Ljava/util/function/Function;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIIIIIII)V")
    private void vanillahud$spritePart(Function<Identifier, RenderType> type, TextureAtlasSprite sprite, int texW, int texH, int u, int v, int x, int y, int w, int h, int color, Operation<Void> original) {
        if (vanillahud$plain(type, sprite.atlasLocation()) && HudCapture.sprite(vanillahud$self(), sprite, texW, texH, u, v, w, h, x, y, w, h, color)) return;
        original.call(type, sprite, texW, texH, u, v, x, y, w, h, color);
    }

    @WrapMethod(method = "innerBlit(Ljava/util/function/Function;Lnet/minecraft/resources/Identifier;IIIIFFFFI)V")
    private void vanillahud$texture(Function<Identifier, RenderType> type, Identifier id, int x0, int x1, int y0, int y1, float u0, float u1, float v0, float v1, int color, Operation<Void> original) {
        if (vanillahud$plain(type, id) && HudCapture.texture(vanillahud$self(), id, x0, x1, y0, y1, u0, u1, v0, v1, color)) return;
        original.call(type, id, x0, x1, y0, y1, u0, u1, v0, v1, color);
    }

    @WrapMethod(method = "fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V")
    private void vanillahud$fill(RenderType type, int x0, int y0, int x1, int y1, int z, int color, Operation<Void> original) {
        if ((type == RenderType.gui() || type == RenderType.guiOverlay()) && HudCapture.fill(vanillahud$self(), x0, y0, x1, y1, color)) return;
        original.call(type, x0, y0, x1, y1, z, color);
    }

    @WrapMethod(method = "enableScissor")
    private void vanillahud$clipStart(int x0, int y0, int x1, int y1, Operation<Void> original) {
        HudCapture.clipStart(vanillahud$self(), x0, y0, x1, y1);
        original.call(x0, y0, x1, y1);
    }

    @WrapMethod(method = "disableScissor")
    private void vanillahud$clipEnd(Operation<Void> original) {
        HudCapture.clipEnd();
        original.call();
    }

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void vanillahud$item(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int offset, Operation<Void> original) {
        if (HudCapture.item(vanillahud$self(), stack, x, y)) return;
        original.call(entity, level, stack, x, y, seed, offset);
    }

    @WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    private int vanillahud$string(Font font, String text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        boolean drop = TextShadowHook.apply(shadow);
        if (text != null && HudCapture.text(vanillahud$self(), text, x, y, color, drop)) return x + font.width(text) + (drop ? 1 : 0);
        return original.call(font, text, x, y, color, shadow);
    }

    @WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I")
    private int vanillahud$sequence(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        boolean drop = TextShadowHook.apply(shadow);
        if (HudCapture.text(vanillahud$self(), text, x, y, color, drop)) return x + font.width(text) + (drop ? 1 : 0);
        return original.call(font, text, x, y, color, shadow);
    }
}
*///?} elif > 1.8.9 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GuiGraphicsExtractor.class, priority = 1500)
public abstract class DrawCaptureMixin {
    @Unique
    private GuiGraphicsExtractor vanillahud$self() {
        return (GuiGraphicsExtractor) (Object) this;
    }

    @Unique
    private static int vanillahud$argb(float r, float g, float b, float a) {
        return ((int) (a * 255f) << 24) | ((int) (r * 255f) << 16) | ((int) (g * 255f) << 8) | (int) (b * 255f);
    }

    // this version tints through the shader colour
    @Unique
    private static int vanillahud$tint() {
        float[] c = RenderSystem.getShaderColor();
        return vanillahud$argb(c[0], c[1], c[2], c[3]);
    }

    @WrapMethod(method = "blitSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIII)V")
    private void vanillahud$sprite(TextureAtlasSprite sprite, int x, int y, int z, int w, int h, Operation<Void> original) {
        if (HudCapture.sprite(vanillahud$self(), sprite, x, y, w, h, vanillahud$tint())) return;
        original.call(sprite, x, y, z, w, h);
    }

    @WrapMethod(method = "blitSprite(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;IIIIIIIII)V")
    private void vanillahud$spritePart(TextureAtlasSprite sprite, int texW, int texH, int u, int v, int x, int y, int z, int w, int h, Operation<Void> original) {
        if (HudCapture.sprite(vanillahud$self(), sprite, texW, texH, u, v, w, h, x, y, w, h, vanillahud$tint())) return;
        original.call(sprite, texW, texH, u, v, x, y, z, w, h);
    }

    @WrapMethod(method = "blit(IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V")
    private void vanillahud$atlasSprite(int x, int y, int z, int w, int h, TextureAtlasSprite sprite, Operation<Void> original) {
        if (HudCapture.sprite(vanillahud$self(), sprite, x, y, w, h, vanillahud$tint())) return;
        original.call(x, y, z, w, h, sprite);
    }

    @WrapMethod(method = "blit(IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;FFFF)V")
    private void vanillahud$atlasSpriteTinted(int x, int y, int z, int w, int h, TextureAtlasSprite sprite, float r, float g, float b, float a, Operation<Void> original) {
        if (HudCapture.sprite(vanillahud$self(), sprite, x, y, w, h, vanillahud$argb(r, g, b, a))) return;
        original.call(x, y, z, w, h, sprite, r, g, b, a);
    }

    @WrapMethod(method = "innerBlit(Lnet/minecraft/resources/Identifier;IIIIIFFFF)V")
    private void vanillahud$texture(Identifier id, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, Operation<Void> original) {
        if (HudCapture.texture(vanillahud$self(), id, x0, x1, y0, y1, u0, u1, v0, v1, vanillahud$tint())) return;
        original.call(id, x0, x1, y0, y1, z, u0, u1, v0, v1);
    }

    @WrapMethod(method = "innerBlit(Lnet/minecraft/resources/Identifier;IIIIIFFFFFFFF)V")
    private void vanillahud$textureTinted(Identifier id, int x0, int x1, int y0, int y1, int z, float u0, float u1, float v0, float v1, float r, float g, float b, float a, Operation<Void> original) {
        if (HudCapture.texture(vanillahud$self(), id, x0, x1, y0, y1, u0, u1, v0, v1, vanillahud$argb(r, g, b, a))) return;
        original.call(id, x0, x1, y0, y1, z, u0, u1, v0, v1, r, g, b, a);
    }

    @WrapMethod(method = "fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V")
    private void vanillahud$fill(RenderType type, int x0, int y0, int x1, int y1, int z, int color, Operation<Void> original) {
        if ((type == RenderType.gui() || type == RenderType.guiOverlay()) && HudCapture.fill(vanillahud$self(), x0, y0, x1, y1, color)) return;
        original.call(type, x0, y0, x1, y1, z, color);
    }

    @WrapMethod(method = "enableScissor")
    private void vanillahud$clipStart(int x0, int y0, int x1, int y1, Operation<Void> original) {
        HudCapture.clipStart(vanillahud$self(), x0, y0, x1, y1);
        original.call(x0, y0, x1, y1);
    }

    @WrapMethod(method = "disableScissor")
    private void vanillahud$clipEnd(Operation<Void> original) {
        HudCapture.clipEnd();
        original.call();
    }

    @WrapMethod(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V")
    private void vanillahud$item(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int offset, Operation<Void> original) {
        if (HudCapture.item(vanillahud$self(), stack, x, y)) return;
        original.call(entity, level, stack, x, y, seed, offset);
    }

    @WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I")
    private int vanillahud$string(Font font, String text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        boolean drop = TextShadowHook.apply(shadow);
        if (text != null && HudCapture.text(vanillahud$self(), text, x, y, color, drop)) return x + font.width(text) + (drop ? 1 : 0);
        return original.call(font, text, x, y, color, shadow);
    }

    @WrapMethod(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I")
    private int vanillahud$sequence(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
        boolean drop = TextShadowHook.apply(shadow);
        if (HudCapture.text(vanillahud$self(), text, x, y, color, drop)) return x + font.width(text) + (drop ? 1 : 0);
        return original.call(font, text, x, y, color, shadow);
    }
}
*///?} else {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiElement;
import org.polyfrost.vanillahud.compat.LegacyDrawContext;
import org.polyfrost.vanillahud.render.HudCapture;
import org.spongepowered.asm.mixin.Mixin;

// 1.8.9 has no draw context, so each kind of draw is caught where it reaches GL: these for quads,
// with the font, item, texture and colour hooks in the Capture mixins beside this one
@Mixin(value = GuiElement.class, priority = 1500)
public abstract class DrawCaptureMixin {
    @WrapMethod(method = "drawTexture(IIIIII)V")
    private void vanillahud$quad(int x, int y, int u, int v, int w, int h, Operation<Void> original) {
        float s = 1f / 256f;
        if (HudCapture.texture(LegacyDrawContext.INSTANCE, x, y, w, h, u * s, v * s, (u + w) * s, (v + h) * s)) return;
        original.call(x, y, u, v, w, h);
    }

    @WrapMethod(method = "drawTexture(IIFFIIFF)V")
    private static void vanillahud$sized(int x, int y, float u, float v, int w, int h, float texW, float texH, Operation<Void> original) {
        if (HudCapture.texture(LegacyDrawContext.INSTANCE, x, y, w, h, u / texW, v / texH, (u + w) / texW, (v + h) / texH)) return;
        original.call(x, y, u, v, w, h, texW, texH);
    }

    @WrapMethod(method = "drawTexture(IIFFIIIIFF)V")
    private static void vanillahud$scaled(int x, int y, float u, float v, int uW, int vH, int w, int h, float texW, float texH, Operation<Void> original) {
        if (HudCapture.texture(LegacyDrawContext.INSTANCE, x, y, w, h, u / texW, v / texH, (u + uW) / texW, (v + vH) / texH)) return;
        original.call(x, y, u, v, uW, vH, w, h, texW, texH);
    }

    @WrapMethod(method = "fill(IIIII)V")
    private static void vanillahud$fill(int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        if (HudCapture.fill(LegacyDrawContext.INSTANCE, x0, y0, x1, y1, color)) return;
        original.call(x0, y0, x1, y1, color);
    }
}
*///?}
