package org.polyfrost.vanillahud.compat

//? if >=1.21.8 {
import net.fabricmc.loader.api.FabricLoader
import org.spongepowered.asm.mixin.FabricUtil
import org.spongepowered.asm.mixin.Mixins
//?}

class MixinOwner(val mixinPackage: String, val modId: String?)

/** Names the mod behind a change to a HUD, from the mixins merged into the draw that made it. */
object ForeignAttribution {
    private val NEVER_BLAMED = setOf("vanillahud", "oneconfig", "mixinextras", "minecraft", "java")
    private val KNOWN_CULPRITS = listOf("noxesium")
    private val OWN_STACK_PREFIXES = listOf("org.polyfrost.vanillahud.", "java.")
    private const val MIXIN_MERGED_ANNOTATION = "MixinMerged"

    private fun isNeverBlamed(modId: String) = modId in NEVER_BLAMED || modId.startsWith("fabric")

    private fun ownerOf(mixin: String, owners: List<MixinOwner>): MixinOwner? =
        owners
            .filter { it.modId != null && mixin.startsWith(it.mixinPackage.trimEnd('.') + ".") }
            .maxByOrNull { it.mixinPackage.length }

    /**
     * The first mod id out of [mixins], innermost first, whose config owns one of them and which is
     * not this mod, OneConfig or Fabric API, else the first of [known] that is [loaded].
     */
    fun modIdOf(
        mixins: List<String>,
        owners: List<MixinOwner>,
        loaded: (String) -> Boolean,
        known: List<String> = KNOWN_CULPRITS,
    ): String? {
        for (mixin in mixins) {
            val modId = ownerOf(mixin, owners)?.modId ?: continue
            if (!isNeverBlamed(modId)) return modId
        }
        return known.firstOrNull(loaded)
    }

    /**
     * The name of the mod behind the draw being made, or null when it can't be told. Walks the stack,
     * so it belongs on a change of state and not on every frame.
     */
    @JvmStatic
    fun attribute(): String? {
        //? if >=1.21.8 {
        return try {
            val loader = FabricLoader.getInstance()
            val owners = Mixins.getConfigs().map { MixinOwner(it.config.mixinPackage, FabricUtil.getModId(it.config, null)) }
            val modId = modIdOf(mixinsMergedIntoStack(), owners, loader::isModLoaded) ?: return null
            loader.getModContainer(modId).map { it.metadata.name }.orElse(modId)
        } catch (_: Throwable) {
            null
        }
        //?} else
        //return null
    }

    //? if >=1.21.8 {
    private fun mixinsMergedIntoStack(): List<String> {
        val loader = Thread.currentThread().contextClassLoader
        val mixins = ArrayList<String>()
        for (frame in Throwable().stackTrace) {
            if (OWN_STACK_PREFIXES.any { frame.className.startsWith(it) }) continue
            try {
                val owner = Class.forName(frame.className, false, loader)
                for (method in owner.declaredMethods) {
                    if (method.name != frame.methodName) continue
                    for (annotation in method.annotations) {
                        val type = annotation.javaClass.interfaces.firstOrNull { it.simpleName == MIXIN_MERGED_ANNOTATION } ?: continue
                        mixins.add(type.getMethod("mixin").invoke(annotation) as String)
                    }
                }
            } catch (_: Throwable) {
            }
        }
        return mixins
    }
    //?}
}
