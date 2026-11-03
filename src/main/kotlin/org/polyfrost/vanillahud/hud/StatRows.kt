package org.polyfrost.vanillahud.hud

//? if > 1.8.9 {
import net.minecraft.world.entity.LivingEntity
//?} else {
/*import net.minecraft.entity.living.LivingEntity
*///?}
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.polyfrost.vanillahud.util.EditorSnapshot
import kotlin.math.ceil
import kotlin.math.max

/**
 * How far vanilla pushes the status bars up once the hearts or a mount's hearts take another row.
 * The bars use it to grow their own box and to keep following the content instead of staying put.
 */
object StatRows {
    private const val ICONS_PER_ROW = 10
    private const val MAX_MOUNT_HEARTS = 30

    /** set by tests since there is no player to read */
    internal var healthRowsOverride: Int? = null
    internal var mountRowsOverride: Int? = null

    fun healthRows(maxHealth: Float, health: Float, absorption: Float): Int =
        ceil((max(maxHealth, health) + ceil(absorption)) / 2f / ICONS_PER_ROW).toInt().coerceAtLeast(1)

    /** vanilla squeezes the rows together from the third one on */
    fun healthRowHeight(rows: Int): Int = max(10 - (rows - 2), 3)

    fun mountRows(hearts: Int): Int = ceil(hearts.coerceIn(0, MAX_MOUNT_HEARTS).toFloat() / ICONS_PER_ROW).toInt()

    /** how many heart rows are drawn, the editor's preview reads the same held values the game is shown there */
    fun healthRows(editing: Boolean): Int {
        healthRowsOverride?.let { return it }
        val player = player() ?: return 1
        if (!editing) {
            return healthRows(player.maxHealth, player.health, absorptionOf(player))
        }
        val maxHealth = EditorSnapshot.hold("maxHealth", player.maxHealth.toDouble(), player.maxHealth > 0, 20.0)
        val health = EditorSnapshot.hold("health", player.health, player.health > 0, 20f)
        val absorption = EditorSnapshot.hold("absorption", absorptionOf(player), true, 0f)
        return healthRows(maxHealth.toFloat(), health, absorption)
    }

    /** how far the armor and the topmost heart row sit above the first row */
    fun healthRise(editing: Boolean): Float {
        val rows = healthRows(editing)
        return ((rows - 1) * healthRowHeight(rows)).toFloat()
    }

    private fun player() = try {
        mc.player
    } catch (_: Throwable) {
        null
    }

    //? if > 1.8.9 {
    private fun absorptionOf(player: LivingEntity) = player.absorptionAmount
    //?} else
    //private fun absorptionOf(player: LivingEntity) = player.absorption

    fun mountRows(): Int {
        mountRowsOverride?.let { return it }
        val mount = player()?.vehicle as? LivingEntity ?: return 0
        if (!mount.isAlive) return 0
        return mountRows(((mount.maxHealth + 0.5f).toInt() / 2))
    }

    /** how far the mount's hearts and the air bubbles above them sit above the first row */
    fun mountRise(): Float = (mountRows() - 1).coerceAtLeast(0) * 10f
}
