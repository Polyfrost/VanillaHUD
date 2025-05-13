package org.polyfrost.vanillahud.utils

import dev.deftu.omnicore.client.OmniClient
import dev.deftu.omnicore.client.render.OmniMatrixStack

/**
 * Random functions that hopefully will make their way into OmniCore in the future
 */

fun OmniMatrixStack.drawScaledString(text: String, x: Float, y: Float, color: Int, type: Int, scale: Float) {
    this.push()
    this.scale(scale, scale, 1f)

    val scaledx = x * (1 / scale)
    val scaledy = y * (1 / scale)

    when(type) {
        0 -> {
            OmniClient.fontRenderer.drawString(text, scaledx, scaledy, color, false)
        }
        1 -> {
            OmniClient.fontRenderer.drawString(text, scaledx, scaledy, color, true)
        }
        2 -> {
            // TODO: Implement full border
            OmniClient.fontRenderer.drawString(text, scaledx, scaledy, color, true)
        }
    }

    this.pop()
}