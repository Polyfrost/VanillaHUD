package org.polyfrost.vanillahud.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.8 {
import net.minecraft.client.renderer.RenderPipelines;
//?} elif >=1.21.4 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
import net.minecraft.client.renderer.texture.DynamicTexture;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.FileInputStream;
import java.io.InputStream;

public final class ScoreboardBackground {
    private ScoreboardBackground() {}

    //? if >=1.21.11 {
    private static final Identifier TEXTURE_ID =
            Identifier.fromNamespaceAndPath("vanillahud", "scoreboard_background");
    //?} else {
    /*private static final ResourceLocation TEXTURE_ID =
            ResourceLocation.fromNamespaceAndPath("vanillahud", "scoreboard_background");
    *///?}

    private static String loadedPath = null;
    private static boolean loaded = false;
    private static int texWidth = 0;
    private static int texHeight = 0;

    public static String chooseFile() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(1);
            filters.put(stack.UTF8("*.png"));
            filters.flip();
            return TinyFileDialogs.tinyfd_openFileDialog(
                    "Select Scoreboard Background", "", filters, "Image Files", false);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean render(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, String path) {
        if (!ensureLoaded(path)) return false;
        int w = x1 - x0;
        int h = y1 - y0;
        if (w <= 0 || h <= 0) return true;
        //? if >=1.21.8 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_ID, x0, y0, 0f, 0f,
                w, h, texWidth, texHeight, texWidth, texHeight);
        //?} elif >=1.21.4 {
        /*graphics.blit(RenderType::guiTextured, TEXTURE_ID, x0, y0, 0f, 0f,
                w, h, texWidth, texHeight, texWidth, texHeight);
        *///?} else {
        /*graphics.blit(TEXTURE_ID, x0, y0, w, h, 0f, 0f, texWidth, texHeight, texWidth, texHeight);
        *///?}
        return true;
    }

    private static boolean ensureLoaded(String path) {
        if (path == null || path.isEmpty()) {
            loadedPath = path;
            loaded = false;
            return false;
        }
        if (path.equals(loadedPath)) return loaded;
        loadedPath = path;
        loaded = false;
        try (InputStream in = new FileInputStream(path)) {
            NativeImage image = NativeImage.read(in);
            texWidth = image.getWidth();
            texHeight = image.getHeight();
            //? if >=1.21.5 {
            DynamicTexture texture = new DynamicTexture(() -> "vanillahud/scoreboard_background", image);
            //?} else {
            /*DynamicTexture texture = new DynamicTexture(image);
            *///?}
            Minecraft.getInstance().getTextureManager().register(TEXTURE_ID, texture);
            loaded = true;
        } catch (Throwable t) {
            loaded = false;
        }
        return loaded;
    }
}
