package org.polyfrost.vanillahud.mixin.skyblock;

import org.polyfrost.oneconfig.internal.hud.HudCore;
import codes.biscuit.skyblockaddons.features.tablist.RenderColumn;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.listeners.RenderListener")
public class RenderListenerMixin {

    @Dynamic
    @Redirect(method = "onRenderRemoveBars", at = @At(value = "INVOKE", target = "Lcodes/biscuit/skyblockaddons/features/tablist/TabListParser;getRenderColumns()Ljava/util/List;"), remap = false)
    private List<RenderColumn> editing() {
        return HudCore.editing ? null : TabListParser.getRenderColumns();
    }

}