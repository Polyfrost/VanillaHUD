plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

stonecutter parameters {
    replacements {
        string(current.parsed >= "1.21.6") {
            replace("pushMatrix", "pushPose")
            replace("popMatrix", "popPose")
        }

        string(current.parsed < "1.21.11") {
            replace("net.minecraft.Util", "net.minecraft.util.Util")
        }
    }
}
