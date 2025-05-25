package org.polyfrost.vanillahud.mixin.minecraft.interfaces;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface ZLevelAccessor_Mixin {

    @Accessor
    float getZLevel();

    @Accessor
    void setZLevel(float zLevel);
}