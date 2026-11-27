package org.polyfrost.vanillahud.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.polyfrost.vanillahud.util.HudConfigMigrator
import java.nio.file.Files

class StrandedBarsTest {
    @Test
    fun `unanchored bars are unmarked even when saved as player-moved`() {
        val folder = Files.createTempDirectory("vanillahud-bars")
        val huds = Files.createDirectories(folder.resolve("huds"))
        Files.writeString(huds.resolve("vanillahud-health.json"), """{ "anchorTargetId": "", "movedByPlayer": true }""")
        Files.writeString(huds.resolve("vanillahud-hunger.json"), """{ "anchorTargetId": "", "movedByPlayer": false }""")
        Files.writeString(huds.resolve("vanillahud-armor.json"), """{ "anchorTargetId": "huds/vanillahud-health.json" }""")
        Files.writeString(huds.resolve("vanillahud-air.json"), "{}")
        val customized = folder.resolve("vanillahud-customized")
        Files.write(
            customized,
            listOf(
                "vanillahud-health.json", "vanillahud-hunger.json", "vanillahud-armor.json", "vanillahud-air.json",
                "vanillahud-experience.json", "vanillahud-tab.json",
            ),
        )

        HudConfigMigrator.reseedStrandedBars(folder)

        assertEquals(listOf("vanillahud-armor.json", "vanillahud-tab.json"), Files.readAllLines(customized))
    }
}
