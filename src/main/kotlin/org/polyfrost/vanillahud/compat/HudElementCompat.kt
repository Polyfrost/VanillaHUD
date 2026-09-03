package org.polyfrost.vanillahud.compat

//? if >=1.21.4 {
import net.minecraft.client.gui.GuiGraphicsExtractor
import org.polyfrost.vanillahud.hud.HotbarHud
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
        // every status and experience layer rides the hotbar element so the cluster moves as one
        replaceIcons(VanillaHudElements.AIR_BAR) { Huds.hotbar }
        replaceIcons(VanillaHudElements.ARMOR_BAR) { Huds.hotbar }
        replaceIcons(VanillaHudElements.HEALTH_BAR) { Huds.hotbar }
        replaceIcons(VanillaHudElements.FOOD_BAR) { Huds.hotbar }
        replaceIcons(VanillaHudElements.MOUNT_HEALTH) { Huds.hotbar }
        replace(VanillaHudElements.INFO_BAR) { Huds.hotbar }
        replaceUpright(VanillaHudElements.EXPERIENCE_LEVEL, { Huds.hotbar }, ::experienceLevelCenter)
        replace(VanillaHudElements.HELD_ITEM_TOOLTIP) { Huds.heldItemTooltip }
        replace(VanillaHudElements.OVERLAY_MESSAGE) { Huds.actionBar }
        replace(VanillaHudElements.TITLE_AND_SUBTITLE) { Huds.title }
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

    /** layers drawn as one block counter rotated whole about [center] so text stays readable */
    private fun replaceUpright(
        id: Identifier,
        hud: () -> VanillaHud,
        center: (GuiGraphicsExtractor) -> Pair<Float, Float>,
    ) = wrap(id, hud, { ctx, element ->
        HudTransform.begin(ctx, element)
        val (cx, cy) = center(ctx)
        HudTransform.beginUpright(ctx, element, cx, cy)
    }, { ctx ->
        HudTransform.endUpright(ctx)
        HudTransform.end(ctx)
    })

    private fun experienceLevelCenter(context: GuiGraphicsExtractor): Pair<Float, Float> =
        context.guiWidth() / 2f to context.guiHeight() - HotbarHud.LEVEL_CENTER_Y

    private fun wrap(
        id: Identifier,
        hud: () -> VanillaHud,
        begin: (GuiGraphicsExtractor, VanillaHud) -> Unit,
        end: (GuiGraphicsExtractor) -> Unit,
    ) {
        HudElementRegistry.replaceElement(id) { original ->
            HudElement { context, tickCounter ->
                val element = hud()
                if (element.shouldDraw()) {
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

object HudElementCompat {
    fun init() {
        HudLayerRegistrationCallback.EVENT.register { layers ->
            replaceUpright(layers, IdentifiedLayer.EXPERIENCE_LEVEL) { Huds.hotbar }
            replace(layers, IdentifiedLayer.OVERLAY_MESSAGE) { Huds.actionBar }
            replace(layers, IdentifiedLayer.TITLE_AND_SUBTITLE) { Huds.title }
            replace(layers, IdentifiedLayer.SCOREBOARD) { Huds.scoreboard }
            replace(layers, IdentifiedLayer.PLAYER_LIST) { Huds.tabList }
            replace(layers, IdentifiedLayer.BOSS_BAR) { Huds.bossBar }
            replace(layers, IdentifiedLayer.SUBTITLES) { Huds.closedCaptions }
        }
    }

    private fun replace(layers: LayeredDrawerWrapper, id: Identifier, hud: () -> VanillaHud) =
        wrap(layers, id, hud, HudTransform::begin, HudTransform::end)

    private fun replaceUpright(layers: LayeredDrawerWrapper, id: Identifier, hud: () -> VanillaHud) =
        wrap(layers, id, hud, { ctx, element ->
            HudTransform.begin(ctx, element)
            HudTransform.beginUpright(
                ctx, element,
                ctx.guiWidth() / 2f, ctx.guiHeight() - HotbarHud.LEVEL_CENTER_Y,
            )
        }, { ctx ->
            HudTransform.endUpright(ctx)
            HudTransform.end(ctx)
        })

    private fun wrap(
        layers: LayeredDrawerWrapper,
        id: Identifier,
        hud: () -> VanillaHud,
        begin: (GuiGraphicsExtractor, VanillaHud) -> Unit,
        end: (GuiGraphicsExtractor) -> Unit,
    ) {
        layers.replaceLayer(id) { original ->
            IdentifiedLayer.of(original.id()) { context, tickCounter ->
                val element = hud()
                if (element.shouldDraw()) {
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
