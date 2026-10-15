package org.polyfrost.vanillahud.render

//? if > 1.8.9 {
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
//? if >=1.21.8 {
import net.minecraft.client.renderer.RenderPipelines
//?} elif >=1.21.4 {
/*import net.minecraft.client.renderer.RenderType
*///?}
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.Identifier
import java.io.FileInputStream

object ScoreboardBackground {
    private val TEXTURE_ID: Identifier =
        Identifier.fromNamespaceAndPath("vanillahud", "scoreboard_background")

    private var loadedPath: String? = null
    private var loaded = false
    private var texWidth = 0
    private var texHeight = 0

    @JvmStatic
    fun render(graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, path: String?): Boolean {
        if (!ensureLoaded(path)) return false
        val w = x1 - x0
        val h = y1 - y0
        if (w <= 0 || h <= 0) return true
        //? if >=1.21.8 {
        graphics.blit(
            RenderPipelines.GUI_TEXTURED, TEXTURE_ID, x0, y0, 0f, 0f,
            w, h, texWidth, texHeight, texWidth, texHeight
        )
        //?} elif >=1.21.4 {
        /*graphics.blit(
            RenderType::guiTextured, TEXTURE_ID, x0, y0, 0f, 0f,
            w, h, texWidth, texHeight, texWidth, texHeight
        )
        *///?} else {
        /*graphics.blit(TEXTURE_ID, x0, y0, w, h, 0f, 0f, texWidth, texHeight, texWidth, texHeight)
        *///?}
        return true
    }

    private fun ensureLoaded(path: String?): Boolean {
        if (path.isNullOrEmpty()) {
            loadedPath = path
            loaded = false
            return false
        }
        if (path == loadedPath) return loaded
        loadedPath = path
        loaded = false
        try {
            FileInputStream(path).use { `in` ->
                val image = NativeImage.read(`in`)
                texWidth = image.width
                texHeight = image.height
                //? if >=1.21.5 {
                val texture = DynamicTexture({ "vanillahud/scoreboard_background" }, image)
                //?} else {
                /*val texture = DynamicTexture(image)
                *///?}
                Minecraft.getInstance().textureManager.register(TEXTURE_ID, texture)
                loaded = true
            }
        } catch (t: Throwable) {
            loaded = false
        }
        return loaded
    }
}
//?} else {
/*import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiElement
import net.minecraft.client.render.platform.GlStateManager
import net.minecraft.client.render.texture.DynamicTexture
import net.minecraft.resources.Identifier
import org.polyfrost.vanillahud.compat.GuiGraphicsExtractor
import java.io.File
import javax.imageio.ImageIO

object ScoreboardBackground {
    private val TEXTURE_ID: Identifier = Identifier("vanillahud", "scoreboard_background")

    private var loadedPath: String? = null
    private var loaded = false
    private var texWidth = 0
    private var texHeight = 0

    @JvmStatic
    fun render(graphics: GuiGraphicsExtractor, x0: Int, y0: Int, x1: Int, y1: Int, path: String?): Boolean {
        if (!ensureLoaded(path)) return false
        val w = x1 - x0
        val h = y1 - y0
        if (w <= 0 || h <= 0) return true
        Minecraft.getInstance().textureManager.bind(TEXTURE_ID)
        GlStateManager.enableTexture()
        GlStateManager.enableBlend()
        GlStateManager.color4f(1f, 1f, 1f, 1f)
        // the image is stretched over the whole region, so the source rect is the entire texture
        GuiElement.drawTexture(
            x0, y0, 0f, 0f, texWidth, texHeight, w, h, texWidth.toFloat(), texHeight.toFloat()
        )
        GlStateManager.disableBlend()
        return true
    }

    private fun ensureLoaded(path: String?): Boolean {
        if (path.isNullOrEmpty()) {
            loadedPath = path
            loaded = false
            return false
        }
        if (path == loadedPath) return loaded
        loadedPath = path
        loaded = false
        try {
            // 1.8.9 has no NativeImage, and its DynamicTexture takes an AWT image directly
            val image = ImageIO.read(File(path)) ?: return false
            texWidth = image.width
            texHeight = image.height
            Minecraft.getInstance().textureManager.register(TEXTURE_ID, DynamicTexture(image))
            loaded = true
        } catch (t: Throwable) {
            loaded = false
        }
        return loaded
    }
}
*///?}
