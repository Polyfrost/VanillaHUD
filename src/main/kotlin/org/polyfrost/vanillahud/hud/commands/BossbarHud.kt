package org.polyfrost.vanillahud.hud.commands

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.OmniMatrixStack
import net.minecraft.entity.boss.BossStatus
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud
import kotlin.math.max


class BossbarHud : LegacyHud() {
    override fun title() = "Scoreboard"
    override fun category() = Category.INFO

    val BAR_WIDTH = 182f

    override var width = 0f
        get() {
            val textWidth = if (this.renderText) OmniClient.fontRenderer.getStringWidth((getText())).toFloat() else 0.0f
            val healthWidth = if (this.renderHealth) this.BAR_WIDTH else 0.0f
            return max(textWidth, healthWidth)
        }

    override var height = 0f
        get() {
            var value = 0f

            if (renderHealth) value += 5f

            if (renderText) value += OmniClient.fontRenderer.FONT_HEIGHT

            if (renderText && renderHealth) value++

            return value
        }

    @Switch(
        title = "Render Text"
    )
    var renderText = true

    @Switch(
        title = "Render Health"
    )
    var renderHealth = true

    @Switch(
        title = "Smooth Health",
        description = "Lerps the health bar to make it smoother."
    )
    var smoothHealth = true

    @Slider(
        title = "Lerp Speed",
        min = 1f,
        max = 1000f
    )
    var lerpSpeed = 100f

    @Slider(
        title = "Bar Position",
        min = 0f,
        max = 100f
    )
    var barPosition = 50f

    override fun render(
        stack: OmniMatrixStack,
        x: Float,
        y: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        stack.push()
        stack.scale(scaleX, scaleY, 1f)
        stack.translate(x / scaleX, y / scaleY, 1f)

        drawHealth()

        if (renderText) {
            drawText()
        }

        stack.pop()
    }

    fun drawHealth() {

    }

    fun drawText() {

    }

    fun getText(): String {
        return if (BossStatus.bossName != null && BossStatus.statusBarTime > 0) {
            BossStatus.bossName
        } else if (!isReal) {
            "Polyfrost Organization"
        } else {
            ""
        }
    }

    override fun update() = true

}