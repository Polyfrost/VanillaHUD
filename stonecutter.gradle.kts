plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

stonecutter parameters {
    // TODO: More replacements to simplify code
    // >=1.21.6 pushMatrix -> pushPose
    // >=1.21.6 popMatrix -> popPose

    replacements.string {
        direction = eval(current.version, "< 1.21.11")
        from = "net.minecraft.Util"
        to = "net.minecraft.util.Util"
    }
}
