plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.4") {
            replace("pushPose", "pushMatrix")
            replace("popPose", "popMatrix")
        }

        string(current.parsed < "1.21.11") {
            replace("net.minecraft.Util", "net.minecraft.util.Util")
        }

        string(current.parsed >= "26") {
            replace("net.minecraft.client.gui.GuiGraphics", "net.minecraft.client.gui.GuiGraphicsExtractor")
            replace("GuiGraphics", "GuiGraphicsExtractor")
        }

        string(current.parsed >= "26.2") {
            replace("ContextualBarRenderer", "ContextualBar")
        }
    }
}
