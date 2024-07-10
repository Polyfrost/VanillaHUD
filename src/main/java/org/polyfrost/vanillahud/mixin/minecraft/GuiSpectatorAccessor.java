package org.polyfrost.vanillahud.mixin.minecraft;

import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiSpectator.class)
public interface GuiSpectatorAccessor {

    @Accessor
    SpectatorMenu getField_175271_i();

    @Invoker("func_175265_c")
    float alpha();

}