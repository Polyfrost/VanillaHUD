package org.polyfrost.vanillahud.hook;

import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >= 26.1 {
import net.minecraft.client.gui.components.PlayerFaceExtractor;
//?} else {
/*import net.minecraft.client.gui.components.PlayerFaceRenderer;
*///?}
import net.minecraft.resources.Identifier;

@SuppressWarnings("InstantiationOfUtilityClass")
public interface HeadHook {
    //~ if >=26 'PlayerFaceRenderer' -> 'PlayerFaceExtractor'
    HeadHook INSTANCE = (HeadHook) new PlayerFaceExtractor();

    void vanillahud$draw(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, int color,
                         boolean hatVisible, boolean flip);
}
