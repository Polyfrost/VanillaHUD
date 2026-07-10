package org.polyfrost.vanillahud.compat

//? if >=1.21.4 {
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.hud.VanillaHud
import net.minecraft.resources.Identifier
//?}

//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import org.polyfrost.vanillahud.render.HudTransform

object HudElementCompat {
    fun init() {
        replace(VanillaHudElements.AIR_BAR) { Huds.air }
        replace(VanillaHudElements.ARMOR_BAR) { Huds.armor }
        replace(VanillaHudElements.HEALTH_BAR) { Huds.health }
        replace(VanillaHudElements.FOOD_BAR) { Huds.hunger }
        replace(VanillaHudElements.MOUNT_HEALTH) { Huds.mountHealth }
        replace(VanillaHudElements.INFO_BAR) { Huds.experienceBar }
        replace(VanillaHudElements.EXPERIENCE_LEVEL) { Huds.experienceLevel }
        replace(VanillaHudElements.HELD_ITEM_TOOLTIP) { Huds.heldItemTooltip }
        replace(VanillaHudElements.OVERLAY_MESSAGE) { Huds.actionBar }
        replace(VanillaHudElements.TITLE_AND_SUBTITLE) { Huds.title }
        replace(VanillaHudElements.SCOREBOARD) { Huds.scoreboard }
        replace(VanillaHudElements.PLAYER_LIST) { Huds.tabList }
        replace(VanillaHudElements.BOSS_BAR) { Huds.bossBar }
        //? if >=26 {
        replace(VanillaHudElements.MOB_EFFECTS) { Huds.statusEffects }
        //?}
        replace(VanillaHudElements.SUBTITLES) { Huds.subtitles }
    }

    private fun replace(id: Identifier, hud: () -> VanillaHud) {
        HudElementRegistry.replaceElement(id) { original ->
            HudElement { context, tickCounter ->
                val element = hud()
                if (element.shouldRender()) {
                    HudTransform.begin(context, element)
                    //? if >=26 {
                    original.extractRenderState(context, tickCounter)
                    //?} else {
                    /*original.render(context, tickCounter)
                    *///?}
                    HudTransform.end(context)
                }
            }
        }
    }
}
//?} elif >=1.21.4 {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import org.polyfrost.vanillahud.render.HudTransform

object HudElementCompat {
    fun init() {
        HudLayerRegistrationCallback.EVENT.register { layers ->
            replace(layers, IdentifiedLayer.EXPERIENCE_LEVEL) { Huds.experienceLevel }
            replace(layers, IdentifiedLayer.OVERLAY_MESSAGE) { Huds.actionBar }
            replace(layers, IdentifiedLayer.TITLE_AND_SUBTITLE) { Huds.title }
            replace(layers, IdentifiedLayer.SCOREBOARD) { Huds.scoreboard }
            replace(layers, IdentifiedLayer.PLAYER_LIST) { Huds.tabList }
            replace(layers, IdentifiedLayer.BOSS_BAR) { Huds.bossBar }
            replace(layers, IdentifiedLayer.SUBTITLES) { Huds.subtitles }
        }
    }

    private fun replace(layers: LayeredDrawerWrapper, id: Identifier, hud: () -> VanillaHud) {
        layers.replaceLayer(id) { original ->
            IdentifiedLayer.of(original.id()) { context, tickCounter ->
                val element = hud()
                if (element.shouldRender()) {
                    HudTransform.begin(context, element)
                    original.render(context, tickCounter)
                    HudTransform.end(context)
                }
            }
        }
    }
}
*///?} else {

/*object HudElementCompat {
    fun init() {}
}
*///?}
