package org.polyfrost.vanillahud.render

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
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.io.FileInputStream

object ScoreboardBackground {
    private val TEXTURE_ID: Identifier =
        Identifier.fromNamespaceAndPath("vanillahud", "scoreboard_background")

    private var loadedPath: String? = null
    private var loaded = false
    private var texWidth = 0
    private var texHeight = 0

    @JvmStatic
    fun chooseFile(): String? {
        try {
            MemoryStack.stackPush().use { stack ->
                val filters = stack.mallocPointer(1)
                filters.put(stack.UTF8("*.png"))
                filters.flip()
                return TinyFileDialogs.tinyfd_openFileDialog(
                    "Select Scoreboard Background", "", filters, "Image Files", false
                )
            }
        } catch (t: Throwable) {
            return null
        }
    }

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
        if (path == null || path.isEmpty()) {
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
