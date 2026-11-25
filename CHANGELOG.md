## 3.5.19
- Fixed health, armor, hunger, air and experience bars stuck in the wrong spot. Bars not anchored to another HUD go back to their default position once
- Needs OneConfig 1.9.3 and PolyCompose 3.0.7

## 3.5.18
- Tab list heads are faster to draw. Each player's head is one cached image, so an open tab list no longer redoes skin work every frame
- "Better Hat Layer" is renamed to "Show 3D Heads" to match Chatting
- Fixed thin lines between scoreboard rows on 1.8.9
- Fixed newly added HUDs being placed as if they were a default-sized box
- Fixed HUD backgrounds getting soft edges or light seams at fractional GUI scales
- Text widths on 1.8.9 now match the font PolyCompose draws with
- Needs OneConfig 1.9.3 and PolyCompose 3.0.7

## 3.5.17
- Fixed the tab list being sized for more players than it shows
- Fixed the air bubbles and mount hearts not moving with their HUDs on 1.8.9
- Fixed the locator bar's markers flickering or disappearing with Gnetum

## 3.5.16
- Fixed the status bars shifting out of place when Noxesium scales the hotbar
- The experience level colour now follows Polytone resource packs
- Fixed the locator bar overlay being covered by the experience bar with Gnetum
- Needs OneConfig 1.9.3 and PolyCompose 3.0.5

## 3.5.15
- More performance improvements: style settings, HUD positions and font widths are cached across frames, and on 1.21.11 and newer HUD items are drawn by the game's own item renderer
- Fixed the boss bar drawing over the tab list on 1.8.9
- Needs OneConfig 1.9.3 and PolyCompose 3.0.5

## 3.5.14
- Large performance improvements, most of all on 1.8.9: HUDs are only redrawn when what they show changes, and text, icons and the scoreboard do far less work each frame
- Fixed the hotbar drawing in a different order than vanilla, which broke mods that draw on top of it
- Fixed status bars staying on screen after a gamemode switch with Gnetum installed

## 3.5.13
- Fixed HUD elements that should be hidden staying on screen after closing the mod menu with Gnetum installed

## 3.5.12
- The hotbar, status bars and experience bar now move together as one group by default, and so do the title and subtitle
- Needs OneConfig 1.9.1 and PolyCompose 3.0.2
- Fixed hotbar items disappearing during the pickup animation
- Fixed hotbar items being rendered again and again during the pickup animation, which also keeps them sharp
- Fixed the hotbar text shadow setting being lost when moving over old configs
- Fixed HUD textures not updating after a resource pack reload on 1.8.9

## 3.5.11
- Updated to OneConfig 1.9.0, which needs PolyCompose installed
- The health, hunger, armor and air bars and the experience bar are now separate HUDs you can move on their own
- Mount health shows in place of the hunger bar, like in vanilla. The separate Mount Health HUD is gone
- The held item name sits above the hotbar and moves with it
- The level number follows the text options
- The HUD editor previews HUDs with sample data, and only uses sample data for HUDs with nothing to show
- Added background image scaling modes to the scoreboard
- Added support for SoundFix closed captions
- Add `VanillaHudApi`, which lets other mods have an element drawn exactly as vanilla draws it
- Fixed scaled titles spilling out of their box
- Fixed status bars not lining up with the rows the game stacks or with the element below them
- Fixed HUD text and icons sitting off the pixel grid or off their box
- Fixed § colours being lost in HUD text
- Fixed HUDs drawing over each other with Gnetum
- Fixed the item pickup animation
- Fixed the experience level text shadow
- Fixed the held item tooltip overlapping other elements
- Fixed the hotbar reversing its slot order when placed on its top side
- Fixed old configs not moving over before the HUDs load
- Performance improvements for the hotbar, scoreboard, boss bar and item names
