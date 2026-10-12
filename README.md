# VanillaHUD

[![Modrinth downloads](https://img.shields.io/modrinth/dt/pWXJaKrX?style=flat&logo=modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/mod/vanillahud)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1147254?style=flat&logo=curseforge&label=CurseForge&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/vanillahud)

VanillaHUD turns the parts of Minecraft's own HUD into elements you can move, scale and hide. It covers the hotbar, action bar, held item tooltip, title, scoreboard, tab list and boss bar, and each one has extra options.

## Features

Every element starts in its vanilla position. Open the OneConfig HUD editor to drag it somewhere else, change its scale or hide it.

### Hotbar

The hotbar moves together with the health, hunger, armor, air, mount health and experience displays.

- Dock it to the bottom, left, top or right edge of the screen. Left and right turn it a quarter turn.
- Slide the selected slot highlight between slots. This is off by default.
- Turn off the health bar animation on damage and healing, and the hunger bar shake.
- Always show hardcore hearts, or never show them.
- Hide the experience bar or the experience level.
- On Minecraft 1.21.8 and newer, draw the locator bar's waypoints over the experience bar. Vanilla replaces the experience bar with them. This is off by default.

### Action bar

- Turn off the rainbow color that the "Now Playing" text uses when a jukebox starts.

### Held item tooltip

- Keep the item name on screen all the time, or make it disappear without a fade.

### Title and subtitle

- "Auto Scale" shrinks a title that is wider than the screen. It is off by default.

### Scoreboard

- Choose when the red score numbers show. The default hides them only when they are consecutive numbers, so a server that uses them for real values keeps them. You can also always hide or always show them.
- Hide the numbers when every line has the same score. This is on by default.
- Change the color of the score numbers.
- Hide the scoreboard title, or keep it on screen when there are no lines.
- Change the background colors of the title and the body.
- Use a PNG image as the scoreboard background.
- Turn the text shadow on.

### Tab list

- Slide the tab list open and closed. The animation takes 400 ms by default and you can set it between 50 and 1000 ms.
- Hold the key to show the tab list, or switch to toggle mode.
- Limit the list to between 10 and 120 players. The default is 80.
- Show ping as a number, colored by six ping ranges. You pick the color of each range.
- Hide ping values of 1 or lower and 999 or higher, which some servers send in place of the real ping.
- Hide the header, the footer, the player heads or the ping.
- Put yourself at the top of the list. This is off by default.
- Change the background colors of the header, body, footer and player rows.
- Turn the text shadow off.

### Boss bar

- Hide the boss name or the health bar.

### Status effects and closed captions

These two elements exist on the Fabric versions only.

- Move, scale or hide the status effect icons.
- Move, scale or hide the closed captions, and change their text color, background color and text shadow.

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2 and 26.3 on Fabric.

VanillaHUD needs [OneConfig](https://modrinth.com/mod/oneconfig) and Fabric Language Kotlin. The Fabric builds also need Fabric API.

## Support

- Report bugs and ask questions on the [Polyfrost Discord](https://polyfrost.org/discord).
- Check [Polyfrost Status](https://status.polyfrost.org/) if a Polyfrost service is down.

## Building

```sh
./gradlew ":26.3:build"
```

Replace `26.3` with any supported Minecraft version. The build needs JDK 21 and JDK 25 installed.
