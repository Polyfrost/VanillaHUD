package org.polyfrost.vanillahud.util

import com.mojang.authlib.GameProfile
//? if > 1.8.9 {
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
//?} else {
/*import com.google.gson.JsonParser
*///?}
import net.minecraft.client.Minecraft
//? if > 1.8.9 {
import net.minecraft.client.multiplayer.PlayerInfo
//?} else {
/*import net.minecraft.client.network.PlayerInfo
*///?}
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.utils.v1.NetworkUtils
import java.util.UUID

object TabListManager {
    private const val DEV_LIST_URL =
        "https://raw.githubusercontent.com/Polyfrost/VanillaHUD/main/tablist_uuids.json"

    private const val UNKNOWN_NAME = "Player"

    private val mc: Minecraft = Minecraft.getInstance()

    private val fallbackUuids = listOf(
        "0b4d470f-f2fb-4874-9334-1eaef8ba4804",
        "c8bf4768-af44-48cb-a259-01e42fb7bc79",
        "0e3ee1e0-f4d2-4550-8fe9-4f7a0d2cd08a",
        "0d68ec06-ec8f-4558-959f-7a6d7efd7fa5",
        "a5331404-0e77-440e-8bef-24c071dac1ae"
    ).map(UUID::fromString)

    @Volatile
    var devInfo: List<PlayerInfo> = emptyList()
        private set

    @Volatile
    private var requested = false

    fun ensureLoaded() {
        if (requested) return
        requested = true
        Multithreading.submit {
            devInfo = fallbackUuids.toPlayerInfoList()
            updateFromNetwork()
        }
    }

    private fun updateFromNetwork() {
        try {
            val input = NetworkUtils.getString(DEV_LIST_URL) ?: return
            //? if > 1.8.9 {
            val uuids = Json.parseToJsonElement(input).jsonArray
                .map { UUID.fromString(it.jsonObject["id"]!!.jsonPrimitive.content) }
            //?} else {
            /*// kotlinx-serialization-json is not on the 1.8.9 classpath, but Minecraft's own gson is
            val uuids = JsonParser.parseString(input).asJsonArray
                .map { UUID.fromString(it.asJsonObject["id"].asString) }
            *///?}
            devInfo = uuids.toPlayerInfoList()
        } catch (e: Exception) {
            RuntimeException("Failed to load VanillaHUD dev list", e).printStackTrace()
        }
    }

    private fun List<UUID>.toPlayerInfoList(): List<PlayerInfo> =
        //? if > 1.8.9 {
        map { PlayerInfo(getProfile(it), false) }
        //?} else
        //map { PlayerInfo(getProfile(it)) }

    private fun getProfile(uuid: UUID): GameProfile {
        val profile = try {
            //? if >=1.21.9 {
            mc.services().sessionService.fetchProfile(uuid, true)?.profile()
            //?} elif > 1.8.9 {
            /*mc.minecraftSessionService.fetchProfile(uuid, true)?.profile()
            *///?} else {
            /*// 1.8.9's authlib fills the profile in place rather than returning a result
            mc.sessionService.fillProfileProperties(GameProfile(uuid, null), true)
            *///?}
        } catch (_: Exception) {
            null
        }
        return profile?.takeIf { it.name != null } ?: GameProfile(uuid, UNKNOWN_NAME)
    }
}
