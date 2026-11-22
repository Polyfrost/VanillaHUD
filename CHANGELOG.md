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
