package org.polyfrost.vanillahud.compat

//? if >=1.21.4 {
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.vanillahud.hud.Huds
import org.polyfrost.vanillahud.hud.VanillaHud
import net.minecraft.resources.Identifier
//?}

//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import org.polyfrost.vanillahud.render.HudTransform
import org.polyfrost.vanillahud.render.TitlePass

object HudElementCompat {
    fun init() {
        replaceIcons(VanillaHudElements.AIR_BAR) { Huds.air }
        replaceIcons(VanillaHudElements.ARMOR_BAR) { Huds.armor }
        replaceIcons(VanillaHudElements.HEALTH_BAR) { Huds.health }
        replaceIcons(VanillaHudElements.FOOD_BAR) { Huds.hunger }
        replaceIcons(VanillaHudElements.MOUNT_HEALTH) { Huds.mountHealth }
        replace(VanillaHudElements.INFO_BAR) { Huds.experienceBar }
        wrap(VanillaHudElements.EXPERIENCE_LEVEL, { Huds.experienceLevel }, HudTransform::beginText, { ctx -> HudTransform.endText(ctx, Huds.experienceLevel) })
        replace(VanillaHudElements.HELD_ITEM_TOOLTIP) { Huds.heldItemTooltip }
        replace(VanillaHudElements.OVERLAY_MESSAGE) { Huds.actionBar }
        HudElementRegistry.replaceElement(VanillaHudElements.TITLE_AND_SUBTITLE) { original ->
            HudElement { context, tickCounter ->
                TitlePass.draw(context) {
                    //? if >=26 {
                    original.extractRenderState(context, tickCounter)
                    //?} else {
                    /*original.render(context, tickCounter)
                    *///?}
                }
            }
        }
        replace(VanillaHudElements.SCOREBOARD) { Huds.scoreboard }
        replace(VanillaHudElements.PLAYER_LIST) { Huds.tabList }
        replace(VanillaHudElements.BOSS_BAR) { Huds.bossBar }
        //? if >=26 {
        replace(VanillaHudElements.MOB_EFFECTS) { Huds.statusEffects }
        //?}
        replace(VanillaHudElements.SUBTITLES) { Huds.closedCaptions }
    }

    private fun replace(id: Identifier, hud: () -> VanillaHud) = wrap(id, hud, HudTransform::begin, HudTransform::end)

    /** layers built from standalone icons where each sprite is kept upright inside the rotation */
    private fun replaceIcons(id: Identifier, hud: () -> VanillaHud) =
        wrap(id, hud, HudTransform::beginIcons, HudTransform::endIcons)

    private fun wrap(
        id: Identifier,
        hud: () -> VanillaHud,
        begin: (GuiGraphicsExtractor, VanillaHud) -> Unit,
        end: (GuiGraphicsExtractor) -> Unit,
        visible: () -> Boolean = { true },
    ) {
        HudElementRegistry.replaceElement(id) { original ->
            HudElement { context, tickCounter ->
                val element = hud()
                if (element.shouldDraw() && visible()) {
                    begin(context, element)
                    try {
                        //? if >=26 {
                        original.extractRenderState(context, tickCounter)
                        //?} else {
                        /*original.render(context, tickCounter)
                        *///?}
                    } finally {
                        end(context)
                    }
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
import org.polyfrost.vanillahud.render.TitlePass

object HudElementCompat {
    fun init() {
        HudLayerRegistrationCallback.EVENT.register { layers ->
            wrap(layers, IdentifiedLayer.EXPERIENCE_LEVEL, { Huds.experienceLevel }, HudTransform::beginText,
                { ctx -> HudTransform.endText(ctx, Huds.experienceLevel) })
            replace(layers, IdentifiedLayer.OVERLAY_MESSAGE) { Huds.actionBar }
            layers.replaceLayer(IdentifiedLayer.TITLE_AND_SUBTITLE) { original ->
                IdentifiedLayer.of(original.id()) { context, tickCounter ->
                    TitlePass.draw(context) { original.render(context, tickCounter) }
                }
            }
            replace(layers, IdentifiedLayer.SCOREBOARD) { Huds.scoreboard }
            replace(layers, IdentifiedLayer.PLAYER_LIST) { Huds.tabList }
            replace(layers, IdentifiedLayer.BOSS_BAR) { Huds.bossBar }
            replace(layers, IdentifiedLayer.SUBTITLES) { Huds.closedCaptions }
        }
    }

    private fun replace(layers: LayeredDrawerWrapper, id: Identifier, hud: () -> VanillaHud) =
        wrap(layers, id, hud, HudTransform::begin, HudTransform::end)

    private fun wrap(
        layers: LayeredDrawerWrapper,
        id: Identifier,
        hud: () -> VanillaHud,
        begin: (GuiGraphicsExtractor, VanillaHud) -> Unit,
        end: (GuiGraphicsExtractor) -> Unit,
        visible: () -> Boolean = { true },
    ) {
        layers.replaceLayer(id) { original ->
            IdentifiedLayer.of(original.id()) { context, tickCounter ->
                val element = hud()
                if (element.shouldDraw() && visible()) {
                    begin(context, element)
                    try {
                        original.render(context, tickCounter)
                    } finally {
                        end(context)
                    }
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
