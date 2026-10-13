package org.polyfrost.vanillahud.mixin;

//? if > 1.8.9 {
//? if > 1.8.9
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >= 26.1 {
import net.minecraft.client.gui.components.PlayerFaceExtractor;
//?} else {
/*import net.minecraft.client.gui.components.PlayerFaceRenderer;
*///?}
//? if >=26 {
import net.minecraft.client.renderer.RenderPipelines;
//?}
import net.minecraft.resources.Identifier;
import org.polyfrost.vanillahud.hook.HeadHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(/*? if >= 26.1 {*/ PlayerFaceExtractor.class /*?} else {*/ /*PlayerFaceRenderer.class *//*?}*/)
public class PlayerFaceRendererMixin implements HeadHook {
    @Shadow
    private static void /*? if >= 26.1 {*/ extractHat /*?} else {*/ /*drawHat *//*?}*/ (GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, boolean flip /*? if >=26 {*/ , int color /*?}*/) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique
    @Override
    public void vanillahud$draw(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, int color,
                         boolean hatVisible, boolean flip){
        int i = 8 + (flip ? 8 : 0);
        int j = 8 * (flip ? -1 : 1);
        graphics.blit(
                //? if >=26 {
                RenderPipelines.GUI_TEXTURED,
                //?}
                texture, x, y, size, size,
                8, i, 8, j, 64, 64
                /*? if >=26 {*/, color /*?}*/
        );
        if (hatVisible) {
            graphics.pose().translate(-0.5F, -0.5F /*? if <26 {*/ /*, 0F *//*?}*/);
            /*? if >= 26.1 {*/ extractHat /*?} else {*/ /*drawHat *//*?}*/ (graphics, texture, x , y , 9, flip /*? if >=26 {*/ , color /*?}*/);
            graphics.pose().translate(0.5F, 0.5F /*? if <26 {*/ /*, 0F *//*?}*/);
        }
    }
}
//?} else {
/*// 1.8.9 has no PlayerFaceRenderer; heads are drawn inline by the tab list
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameGui.class)
public class PlayerFaceRendererMixin {
}
*///?}
