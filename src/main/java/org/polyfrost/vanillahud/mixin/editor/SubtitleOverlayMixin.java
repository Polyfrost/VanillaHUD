package org.polyfrost.vanillahud.mixin.editor;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.audio.ListenerTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.vanillahud.util.DemoData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {
    @Unique
    private static boolean vanillahud$editing() {
        return HudManager.INSTANCE.isEditing();
    }

    @Inject(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At("HEAD"))
    private void vanillahud$forceSubtitles(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!vanillahud$editing()) return;

        ListenerTransform transform = Minecraft.getInstance().getSoundManager().getListenerTransform();
        if (transform == null) return;

        SubtitleOverlay self = (SubtitleOverlay) (Object) this;
        for (DemoData.DemoSubtitle demo : DemoData.INSTANCE.demoSubtitles(transform.position(), transform.forward(), transform.right())) {
            self.onPlaySound(demo.getSound(), demo.getEvent(), demo.getRange());
        }
    }

    @ModifyExpressionValue(
            //? if <26 {
            /*method = "render",
            *///?} else {
            method = "extractRenderState",
            //?}
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"),
            slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;getListenerTransform()Lcom/mojang/blaze3d/audio/ListenerTransform;"))
    )
    private Object vanillahud$forceSubtitleOption(Object original) {
        return vanillahud$editing() ? Boolean.TRUE : original;
    }
}
