package org.polyfrost.vanillahud.mixin.access;

//? if > 1.8.9 {
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
//?} else {
/*import net.minecraft.client.gui.GameGui;
*///?}
import org.spongepowered.asm.mixin.Mixin;
//? if > 1.8.9 {
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;
//?}

// 1.8.9 drives its single boss bar from static state on BossBar rather than a map of events,
// so there is nothing to reach into; the interface stays so the mixin config does not change
//? if > 1.8.9 {
@Mixin(BossHealthOverlay.class)
//?} else {
/*@Mixin(GameGui.class)
*///?}
public interface IBossHealthOverlay {
    //? if > 1.8.9 {
    @Accessor("events")
    Map<UUID, LerpingBossEvent> getEvents();
    //?}
}
