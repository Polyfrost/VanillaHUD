package org.polyfrost.vanillahud.compat

//? if = 1.8.9 {
/*import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.sound.SoundCategory
import net.minecraft.client.sound.SoundPool
import net.minecraft.client.sound.instance.SimpleSoundInstance
import net.minecraft.resources.Identifier
import org.polyfrost.vanillahud.mixin.compat.soundfix.ISubtitleEntry
import org.polyfrost.vanillahud.mixin.compat.soundfix.ISubtitlesHud
import kotlin.math.cos
import kotlin.math.sin

object SoundFixCaptions {
    val loaded: Boolean = FabricLoader.getInstance().isModLoaded("soundfix")

    private var overlay: ISubtitlesHud? = null

    private var audibleHoldsDemoEntries = false

    private val demoLines = listOf(
        "mob.skeleton.say" to 1, "mob.zombie.say" to -1, "step.stone" to 0,
        "game.player.swim" to 1, "game.player.swim.splash" to -1,
    )

    @JvmStatic
    fun attach(hud: ISubtitlesHud) {
        overlay = hud
    }

    fun texts(): List<String> =
        overlay?.audibleSubtitles?.mapNotNull { (it as? ISubtitleEntry)?.subtitleText }.orEmpty()

    fun hasLiveData() = overlay?.let { it.showSubtitles && it.subtitles.isNotEmpty() } == true

    @JvmStatic
    fun render(hud: ISubtitlesHud, previewing: Boolean, draw: Runnable) {
        val player = Minecraft.getInstance().player
        if (!previewing || player == null) {
            if (audibleHoldsDemoEntries) {
                hud.audibleSubtitles.clear()
                audibleHoldsDemoEntries = false
            }
            draw.run()
            return
        }
        audibleHoldsDemoEntries = true
        val realSubtitles = ArrayList(hud.subtitles)
        val realShowSubtitles = hud.showSubtitles
        hud.subtitles.clear()
        hud.showSubtitles = true
        val yaw = Math.toRadians(player.yaw.toDouble())
        val forwardX = -sin(yaw)
        val forwardZ = cos(yaw)
        val rightX = -cos(yaw)
        val rightZ = -sin(yaw)
        for ((key, direction) in demoLines) {
            val id = Identifier(key)
            val offsetX = if (direction == 0) rightX else forwardX * direction
            val offsetZ = if (direction == 0) rightZ else forwardZ * direction
            val sound = SimpleSoundInstance(
                id, 1f, 1f,
                (player.x + offsetX * DEMO_DISTANCE).toFloat(), player.y.toFloat(), (player.z + offsetZ * DEMO_DISTANCE).toFloat(),
            )
            hud.playSound(sound, SoundPool(id, 1.0, 1.0, SoundCategory.MASTER), Float.POSITIVE_INFINITY)
        }
        try {
            draw.run()
        } finally {
            hud.subtitles.clear()
            hud.subtitles.addAll(realSubtitles)
            hud.showSubtitles = realShowSubtitles
        }
    }

    private const val DEMO_DISTANCE = 8
}
*///?}
