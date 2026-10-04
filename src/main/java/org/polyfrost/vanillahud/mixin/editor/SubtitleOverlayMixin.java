package org.polyfrost.vanillahud.mixin.editor;

//? if > 1.8.9 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.audio.ListenerTransform;
import net.minecraft.client.Minecraft;
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import org.polyfrost.vanillahud.hud.Huds;
import org.polyfrost.vanillahud.mixin.access.ISubtitleOverlay;
import org.polyfrost.vanillahud.util.DemoData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.ArrayList;
import java.util.List;

@Mixin(SubtitleOverlay.class)
public abstract class SubtitleOverlayMixin {
    @Unique
    private static boolean vanillahud$editing() {
        return Huds.INSTANCE.getClosedCaptions().getPreviewing();
    }

    @Unique
    private boolean vanillahud$forcedSubtitles;

    @WrapMethod(
            //? if <26 {
            /*method = "render"
            *///?} else {
            method = "extractRenderState"
            //?}
    )
    private void vanillahud$forceSubtitles(GuiGraphicsExtractor graphics, Operation<Void> original) {
        ISubtitleOverlay overlay = (ISubtitleOverlay) (Object) this;
        ListenerTransform transform = Minecraft.getInstance().getSoundManager().getListenerTransform();
        if (!vanillahud$editing() || transform == null) {
            if (vanillahud$forcedSubtitles) {
                overlay.getAudibleSubtitles().clear();
                vanillahud$forcedSubtitles = false;
            }
            original.call(graphics);
            return;
        }

        vanillahud$forcedSubtitles = true;
        List<Object> subtitles = overlay.getSubtitles();
        List<Object> real = new ArrayList<>(subtitles);
        subtitles.clear();
        SubtitleOverlay self = (SubtitleOverlay) (Object) this;
        for (DemoData.DemoSubtitle demo : DemoData.INSTANCE.demoSubtitles(transform.position(), transform.forward(), transform.right())) {
            self.onPlaySound(demo.getSound(), demo.getEvent(), demo.getRange());
        }
        original.call(graphics);
        subtitles.clear();
        subtitles.addAll(real);
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
//?} else {
/*// 1.8.9 has no subtitles to preview
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public abstract class SubtitleOverlayMixin {
}
*///?}
