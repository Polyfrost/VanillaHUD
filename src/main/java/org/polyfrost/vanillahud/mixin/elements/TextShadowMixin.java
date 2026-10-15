package org.polyfrost.vanillahud.mixin.elements;

//? if > 1.8.9 {
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.vanillahud.hook.TextShadowHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphicsExtractor.class)
public class TextShadowMixin {
    //? if >=26 {
    @ModifyVariable(
            method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private boolean vanillahud$sequenceShadow(boolean dropShadow) {
        return TextShadowHook.apply(dropShadow);
    }
    //?}

    //? if >=1.21.8 && <26 {
    /*@ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private boolean vanillahud$sequenceShadow(boolean dropShadow) {
        return TextShadowHook.apply(dropShadow);
    }

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private boolean vanillahud$stringShadow(boolean dropShadow) {
        return TextShadowHook.apply(dropShadow);
    }
    *///?}

    //? if <1.21.8 {
    /*@ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private boolean vanillahud$sequenceShadow(boolean dropShadow) {
        return TextShadowHook.apply(dropShadow);
    }

    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private boolean vanillahud$stringShadow(boolean dropShadow) {
        return TextShadowHook.apply(dropShadow);
    }
    *///?}
}
//?} else {
/*// the draw context this hooks does not exist on 1.8.9, where text shadow is a draw
// argument rather than a context flag, so the option has no effect there yet
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class TextShadowMixin {
}
*///?}
