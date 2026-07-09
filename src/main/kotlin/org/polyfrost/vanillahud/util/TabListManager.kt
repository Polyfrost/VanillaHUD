package org.polyfrost.vanillahud.util

import com.mojang.authlib.GameProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.PlayerInfo
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.utils.v1.NetworkUtils
import java.util.UUID

object TabListManager {
    private const val DEV_LIST_URL =
        "https://raw.githubusercontent.com/Polyfrost/VanillaHUD/main/tablist_uuids.json"

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
            val uuids = Json.parseToJsonElement(input).jsonArray
                .map { UUID.fromString(it.jsonObject["id"]!!.jsonPrimitive.content) }
            devInfo = uuids.toPlayerInfoList()
        } catch (e: Exception) {
            RuntimeException("Failed to load VanillaHUD dev list", e).printStackTrace()
        }
    }

    private fun List<UUID>.toPlayerInfoList(): List<PlayerInfo> =
        map { PlayerInfo(getProfile(it), false) }

    private fun getProfile(uuid: UUID): GameProfile =
        try {
            //? if >=1.21.9 {
            mc.services().sessionService.fetchProfile(uuid, true)?.profile() ?: GameProfile(uuid, null)
            //?} else {
            /*mc.minecraftSessionService.fetchProfile(uuid, true)?.profile() ?: GameProfile(uuid, null)
            *///?}
        } catch (_: Exception) {
            GameProfile(uuid, null)
        }
}
