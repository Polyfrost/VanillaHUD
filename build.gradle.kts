plugins {
    id("dev.kikugie.loom-back-compat")
}

val modId = property("mod.id").toString()
val modName = property("mod.name").toString()
val modVersion = property("mod.version").toString()
val minecraftVersion = property("minecraft_version").toString()
val minecraftVersionRange = property("minecraft_version_range").toString()
val loaderVersion = property("loader_version").toString()
val oneConfigVersion = property("oneconfig_version").toString()

group = property("mod.group").toString()
version = "$modVersion+$minecraftVersion"

base {
    archivesName.set("$modId-$modVersion+$minecraftVersion")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()

    maven("https://maven.parchmentmc.org")
    maven("https://maven.fabricmc.net")
    maven("https://maven.gegy.dev/releases")
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://maven.deftu.dev/releases")
    maven("https://jitpack.io") {
        content { includeGroupAndSubgroups("com.github") }
    }
}

loom {
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }

    runConfigs.remove(runConfigs["server"])
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")

    val hasOfficialMappings = findProperty("has_official_mappings")?.toString()?.toBoolean() ?: true
    if (hasOfficialMappings) {
        @Suppress("UnstableApiUsage")
        mappings(loom.layered {
            officialMojangMappings()
            optionalProp("parchment_version") {
                parchment("org.parchmentmc.data:parchment-$minecraftVersion:$it@zip")
            }
            optionalProp("yalmm_version") {
                mappings("dev.lambdaurora:yalmm-mojbackward:$minecraftVersion+build.$it")
            }
        })
    } else {
        optionalProp("mappings_version") {
            mappings(it)
        }
    }

    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    implementation("org.polyfrost.oneconfig:commands:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:config:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:config-impl:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:events:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:hud:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:notifications:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:ui:$oneConfigVersion")
    implementation("org.polyfrost.oneconfig:utils:$oneConfigVersion")
}

tasks.processResources {
    val hudMixin = when {
        stonecutter.current.parsed >= "26.2" -> "minecraft.HudMixin"
        stonecutter.current.parsed >= "26" -> "minecraft.GuiExtractorMixin"
        else -> "minecraft.GuiMixin"
    }
    val props = mapOf(
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_version" to modVersion,
        "minecraft_version_range" to minecraftVersionRange,
        "loader_version" to loaderVersion,
        "mixin_classes" to "    \"$hudMixin\",\n    \"minecraft.BossHealthOverlayMixin\""
    )

    inputs.properties(props)

    filesMatching("fabric.mod.json") {
        expand(props)
    }
    filesMatching("mixins.vanillahud.json") {
        expand(props)
    }
}

val javaVersion = findProperty("java_version")?.toString()?.toInt() ?: 21

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion)
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

fun optionalProp(name: String, block: (String) -> Unit) {
    findProperty(name)
        ?.toString()
        ?.takeUnless { it.isBlank() || it == "[VERSIONED]" }
        ?.let(block)
}
