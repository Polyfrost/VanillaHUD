package org.polyfrost.vanillahud.hook;

//? if >= 26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
*///?}
//? if >= 1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
 *///?}

public interface HeadHook {
    @SuppressWarnings("InstantiationOfUtilityClass")
    //~ if >=26 'PlayerFaceRenderer' -> 'PlayerFaceExtractor'
    HeadHook INSTANCE = (HeadHook) new PlayerFaceExtractor();

    void vanillahud$draw(/*? if >= 26.1 {*/ GuiGraphicsExtractor /*?} else {*/ /*GuiGraphics *//*?}*/ graphics, /*? if >= 1.21.11 {*/ Identifier /*?} else {*/ /*ResourceLocation *//*?}*/ texture, int x, int y, int size, int color, boolean hatVisible, boolean flip);
}
