plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.8" /* [SC] DO NOT EDIT */

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.4") {
            replace("pushPose", "pushMatrix")
            replace("popPose", "popMatrix")
        }

        string(current.parsed < "1.21.11") {
            replace("net.minecraft.Util", "net.minecraft.util.Util")
        }
    }
}
