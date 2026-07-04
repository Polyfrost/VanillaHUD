package org.polyfrost.vanillahud.hud

//? if <26 {
import org.polyfrost.oneconfig.utils.v1.dsl.mc
//?}
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch

class HotbarHud : VanillaHud("vanillahud/hotbar.json", "Hotbar", Category.PLAYER) {
    override val naturalWidth get() = 182f
    override val naturalHeight get() = 22f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 22f
}

class HealthHud : VanillaHud("vanillahud/health.json", "Health", Category.PLAYER) {
    // TODO: Implement animation, Ask Wyvest
    @Switch(title = "Health Animation", description = "Animate the health bar when taking damage / healing.")
    var animation = true

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f
}

class ArmorHud : VanillaHud("vanillahud/armor.json", "Armor", Category.PLAYER) {
    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 49f
}

class HungerHud : VanillaHud("vanillahud/hunger.json", "Hunger", Category.PLAYER) {
    // TODO: Implement animation, Ask Wyvest
    @Switch(title = "Hunger Animation", description = "Animate the hunger bar when it shakes.")
    var animation = true

    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f
}

class AirHud : VanillaHud("vanillahud/air.json", "Air", Category.PLAYER) {
    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 49f
}

class MountHealthHud : VanillaHud("vanillahud/mount.json", "Mount Health", Category.PLAYER) {
    override val naturalWidth get() = 81f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f + 10f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 39f
}

class ExperienceBarHud : VanillaHud("vanillahud/experience.json", "Experience Bar", Category.PLAYER) {
    override val naturalWidth get() = 182f
    override val naturalHeight get() = 5f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 29f
}

class ExperienceLevelHud : VanillaHud("vanillahud/experience-level.json", "Experience Level", Category.PLAYER) {
    override val naturalWidth get() = 16f
    override val naturalHeight get() = 9f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 8f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 35f
}

class ActionBarHud : VanillaHud("vanillahud/actionbar.json", "Action Bar", Category.INFO) {
    override val exampleText get() = "Action Bar"
    override val naturalWidth get() = 60f
    override val naturalHeight get() = 11f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 72f
}

class HeldItemTooltipHud : VanillaHud("vanillahud/itemtooltip.json", "Held Item Tooltip", Category.INFO) {
    override val exampleText get() = "Diamond Sword"
    override val naturalWidth get() = 70f
    override val naturalHeight get() = 11f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight - 59f
}

class TitleHud : VanillaHud("vanillahud/title.json", "Title & Subtitle", Category.INFO) {
    override val naturalWidth get() = 120f
    override val naturalHeight get() = 68f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - width / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - 40f

    override fun measuredWidth(): Float {
        //? if <26 {
        return try {
            maxOf(mc.font.width("Title") * 4, mc.font.width("Subtitle") * 2).toFloat()
        } catch (_: Throwable) {
            naturalWidth
        }
        //?} else {
        /*return naturalWidth*/
        //?}
    }
}

class ScoreboardHud : VanillaHud("vanillahud/scoreboard.json", "Scoreboard", Category.INFO) {
    override val naturalWidth get() = 90f
    override val naturalHeight get() = 90f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth - naturalWidth - 1f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = screenHeight / 2f - naturalHeight / 2f
}

class TabListHud : VanillaHud("vanillahud/tab.json", "Tab List", Category.INFO) {
    // TODO: Implement player limit
    @Slider(title = "Tab Player Limit", description = "How many players can display on the tab list.", min = 10f, max = 120f)
    var playerLimit = 80

    override val naturalWidth get() = 200f
    override val naturalHeight get() = 100f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - naturalWidth / 2f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 10f
}

class BossBarHud : VanillaHud("vanillahud/bossbar.json", "Boss Bar", Category.COMBAT) {
    override val naturalWidth get() = 182f
    override val naturalHeight get() = 30f
    override fun vanillaOriginX(screenWidth: Int, screenHeight: Int) = screenWidth / 2f - 91f
    override fun vanillaOriginY(screenWidth: Int, screenHeight: Int) = 12f
}

object Huds {
    val hotbar = HotbarHud()
    val health = HealthHud()
    val armor = ArmorHud()
    val hunger = HungerHud()
    val air = AirHud()
    val mountHealth = MountHealthHud()
    val experienceBar = ExperienceBarHud()
    val experienceLevel = ExperienceLevelHud()
    val actionBar = ActionBarHud()
    val heldItemTooltip = HeldItemTooltipHud()
    val title = TitleHud()
    val scoreboard = ScoreboardHud()
    val tabList = TabListHud()
    val bossBar = BossBarHud()

    val all: Array<VanillaHud>
        get() = arrayOf(
            hotbar, health, armor, hunger, air, mountHealth,
            experienceBar, experienceLevel, actionBar, heldItemTooltip,
            title, scoreboard, tabList, bossBar,
        )
}
