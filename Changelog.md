# Modular Routers - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/desht/ModularRouters/commits for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.18.1

### 9.0.2 (24 Jan 2022)

* This release adds compatibility with most recent 1.18.1 Forge builds (39.0.59 at time of writing)
  * Forge 39.0.55 minimum is required
* Also includes some fixes and improvements forward ported from the last 1.16.5 release
* The Breaker Module can now be configured to filter by block, rather than by dropped items
  * By dropped items remains the default behaviour; the Breaker Module GUI now has a toggle button to change this
* There is now a separate configuration value for blacklisting entities from Activator Module attack mode
  * Config setting is `activatorEntityAttackBlacklist`, which is in addition to existing `activatorEntityBlacklist`
  * This blacklist is empty by default, so Activator Module *will* by default now attack villagers
* Activator module blacklists (`activatorEntityBlacklist` and `activatorEntityAttackBlacklist`) can now also take entity tags
  * Use a '#' prefix to indicate a tag, e.g. `#minecraft:skeletons` will match all skeletons (including Strays and Wither Skeletons)
* Fixed Activator Module running with no item in buffer even when module has a non-empty whitelist filter
* Fixed Breaker Module breaking blocks with no drops even when module has a non-empty whitelist filter
* Fixed Detector Module always emitting redstone, even if router is not in "redstone always" mode
  * One issue to be aware of: if a Detector is already emitting, switching router into "Redstone high" mode won't stop it running, but removing and replacing the Detector module will work around this.
* Fixed graphical glitch in GUI energy widget related to integer arithmetic overflow (only visible with boosted energy values in mod config)

### 9.0.1 (20 Dec 2021)

* Now building with Forge 39.x (for MC 1.18.1)
  * Note that previous 9.0.0 release for MC 1.18 will run on MC 1.18.1, but Patchouli support is broken in that release
  * Patchouli book now works properly

## Minecraft 1.18

### 9.0.0 (6 Dec 2021)

* Just a direct port of 8.0.1 to Minecraft 1.18. No new features or functionality.

## Minecraft 1.17.1

### 8.0.1 (2 Sep 2021)

* This version *requires* Forge 37.0.34 or later
* Breaker Module recipe should now work with any item claiming to act like a pickaxe
* Fixed Puller module ignoring Regulator Upgrade values under some circumstances  
* Modular Router block now requires Iron Pickaxe to break (same as an Iron Block), and the effective tool is a pickaxe
  * Using MC 1.17 block tags for mineability & harvest level
* The One Probe is now supported again (Modular Router block shows extra info in TOP) - TOP 4.0.2 required
* JEI supported partially restored (dragging items from JEI to filters is disabled, due to JEI crash)
* Router should now be rotatable by other mods' wrenches  
* A lot of internal code cleanup

### 8.0.0 (26 Jul 2021)

* Initial alpha release for Minecraft 1.17.1!
* Only basic testing has been done so far; this should be considered very much an unstable alpha at this point
* No JEI or TOP integration since those mods haven't been released yet for 1.17
* Patchouli also unavailable for now
* Feature-wise, this is identical to the last 1.16.5 release with one limitation: only vanilla pickaxes are currently supported in the Breaker Module recipe

## Minecraft 1.16.3 / 1.16.4 / 1.16.5

### 7.5.0 (19 May 2021)
* New Upgrade: the Energy Upgrade!
  * Gives the router an internal energy buffer; each upgrade adds 50,000 FE capacity and 1,000 FE transfer per router tick
  * Can have up to 64 in a router
* New Modules: Energy Output and Energy Distributor!
  * Energy Output can push FE to an adjacent energy-receiving block
  * Energy Distributor can wirelessly push FE to up to 8 nearby energy-receiving blocks
  * Speed Upgrades increase transfer rate for these modules
* Modules can now be configured to require FE to run (on a per-module basis)
  * See "Energy Costs" section in `modularrouters-common.toml`
  * All modules have a default cost of 0 FE, as before (but see Activator Module changes below)
  * Could be useful for modpack makers, e.g. for expert-level packs
* Activator Module now has an "Attack Entity" mode!
  * Router will use the item in the buffer (or empty hand) to attack nearby entities
  * This costs FE by default: 150 FE per attack
  * Speed Upgrades will speed up attacks, BUT weapon cooldowns still apply so don't overuse Speed Upgrades!
  * A Security Upgrade will protect the owner and any whitelisted players from being attacked
* Routers are now called "Modular Router" in-game instead of "Item Router"
  * Since they can handle more than just items...
  * Internal registry name is still `modularrouters:item_router`, so no compat problems for existing worlds; this is just a cosmetic change
* Breaker and Extruder Mk1 modules will now drop XP orbs if the block being broken drops XP (as various ores do)
  * Can be disabled in config if previous behaviour is preferred: see `blockBreakXPDrops`
* Fixed GUI's getting locked up if another player breaks a router while a GUI is open
* Fixed missing recipe for the Filter Round Robin Augment
* Fixed sneak-right-clicking Sync Upgrade displaying the previous sync value, not the new one
* Inspection Filter: return -1 for "missing" properties instead of 0
  * e.g. for durability tests, a durability of <1% returns 0, but an item without durability (e.g. Cobblestone) returns -1
  * this fixes a problem where nearly broken items couldn't be distinguished from items without durability 
* A few Activator Module raytracing tweaks for more intuitive use of Above & Below look modes
  * In particular, routers can now plant things on top of a solid block next to or above the router with Look Above mode
  * In 7.4.0 this required Look Below and only worked for blocks horizontally adjacent, which was very unintuitive
* Lots of updates to the Patchouli manual to reflect new and updated functionality
  
### 7.4.0 (26 Mar 2021)
* Activator Module rework
  * No longer any separate Item/Block modes (just activate item or right-click entity)
  * Now acts just as if an ordinary player right-clicks an item, using same vanilla logic
  * Tests indicate it Does The Right Thing (tm), but there is a possibility of some subtle behaviour changes from older releases - worth checking your Activators to be sure they're still doing what you think!
* Distributor Modules can now run in reverse, i.e. pulling items from multiple inventories
  * Configure this in the module GUI
  * Default is to send items, as before
* Security Upgrades now make router fake players (e.g. for Activator/Placer/Breaker modules) run with the same game profile as the Security Upgrade's owner (i.e. you!)
  * This can be very useful if the Item Router is to run in a protected area (e.g. FTB Chunks)
* Added new Filter Round Robin Augment
  * When installed in a module, the module picks a single item from its filter in turn to filter on, in a round-robin fashion, advancing each time the module executes
  * This is instead of the default behaviour of just filtering on any item in the filter
* The Termination button in module GUI's is now a tri-state instead of a toggle:
  * Off: subsequent modules always run
  * T+: subsequent modules run IFF this module did some work
  * T-: subsequent modules run IFF this module did NOT do any work
* Blacklisted Chisels & Bits blocks from being used as Router or Extruder Mk2 camouflage (they tend to cause crashes when used as camo)  
* Fixed item dupe bug with Bulk Filter GUI
* Fixed typing into the regulation field (when using Regulator Augment) not properly updating the module
* Fixed server crash related to unconfigured Security Upgrades (e.g. pulled straight from JEI)  

### 7.3.1 (21 Jan 2021)
* Fixed crash (intermod compat issue) when placing block with Extruder Mk2 and Mimic Augment
* Activator Module in Entity mode now ignores Villagers and Wandering Traders
  * Activating them locks up their GUI, making it impossible to trade with them
  * Entity blacklist is editable in config: see `activatorEntityBlacklist` in modularrouters-common.toml
* Corrected Regex Filter tooltip: make it clear that regex filters are applied to the item's *registry name*, not its display name

### 7.3.0 (4 Jan 2021)
* A full texture update for the mod, courtesy of texture artist Ridanisaurus!
  * This is a new set of 16x16 textures with a nice Minecrafty feel; integrates much more naturally with vanilla textures (IMHO)
* Holding an Item Router in main or offhand now highlights nearby (<16 blocks) camouflaged Item Routers - https://github.com/desht/ModularRouters/issues/100
  * Can be disabled in client config: `heldRouterShowsCamoRouters`
* Fixed lockup when closing module GUI's if inventory key is rebound to Tab - https://github.com/desht/ModularRouters/issues/102
* Some more Activator Module fixes (particularly when the module is set to look up or down) - https://github.com/desht/ModularRouters/issues/95
* Fixed Activator Module crashes in activating blocks/items (crashes could be from another mod) taking down the whole server - https://github.com/desht/ModularRouters/issues/107
  * Any such crashes are now intercepted and the block/item in question is blacklisted from activation until the next server restart
* Updated the Regex Filter Patchouli manual page to make it clear that regex matches happen against item *registry* names, not *display* names
  * Remember, you can use F3+H to show any item's registry name in the item tooltip

### 7.2.0 (4 Nov 2020)
* Added the Creative Module
  * Creative-only module which just adds items it sees in the module filter to the router's buffer, a way to infinitely produce items
* Support for Minecraft 1.16.4, which appears to be very much compatible with 1.16.3.
  * This release will run on both 1.16.3 and 1.16.4

## Minecraft 1.16.3

### 7.1.1 (22 Oct 2020)
* Fixed the "x" delete buttons sometimes not showing on filter GUI's (mod, regex, inspection)
* Fixed sync issue with configuring a filter which is in an uninstalled module (i.e. module in hand, not router)

7.1.0 is pretty much the same as 7.0.2 for MC 1.16.1 with one fix

### 7.1.0 (4 Oct 2020)
* Fixed client crash when dragging fluids from JEI into the filter slots of a Fluid Module
  * It now places a bucket of that fluid into the filter, if possible

## Minecraft 1.16.1

### 7.0.3 (unreleased)
* Fixed client crash when dragging fluids from JEI into the Fluid Module filter
  * This will now put a bucket of that fluid into the filter if possible
  
### 7.0.2 (21 Sep 2020)
* Activator Module can now run with no item in the router (only really useful when in "Right Click Block" mode, of course)
* Activator Module: internal raytracing improvements
  * In particular, activating an empty bottle against water will now produce a water bottle, as expected
* Activator Module: extra items produced by activating an item are now dropped in-world instead of lost
  * e.g. Right-clicking a stack of empty buckets against a cow produces extra milk buckets
* Fixed a divide-by-0 crash with some fluid tanks which report their max capacity as 0 mB
* Added support for Industrial Foregoing Essence for the Vacuum Module / XP Augment
* Fluid Module with Regulator Augment can now regulate by absolute fluid amount (mB) in addition to percentage of tank capacity
  * Selectable in Fluid Module GUI when Regulator Augment is installed

### 7.0.1 (12 Aug 2020)
* Update zh_cn translations
* Added back Waila/Hwyla support
* Fix crash with Puller Module pulling from Industrial Foregoing Mob Slaughter Factory (possibly other inventories too)
* Fix crash with unconfigured Fluid Module Mk2 under some circumstances

### 7.0.0 (11 Jul 2020)
* Initial release for Minecraft 1.16!  This is largely feature-equivalent to the last 1.15.2 release (6.1.1)
* Support for JEI, The One Probe and Patchouli is in place.  Support for Hwyla will be added back when it gets a 1.16.1 Forge port.
* Items can now be dragged from JEI directly to filter slots in module GUI's.  No need to have those items in your inventory anymore.

## Minecraft 1.15.2

### 6.1.1 (22 Jun 2020)
* Fixed energy/fluid dupe issues with more than one of a stackable energy or fluid containing item in the router's buffer
  * Fluid/energy capabilities are now only offered with a single item in the buffer
* Fixed recipes for Breaker and Extruder Mk1 modules not showing in JEI

### 6.1.0 (12 May 2020)
* Added support for Cyclic XP Juice in the Vacuum Module (XP augment)
* Added config option to not display "flying" items when they're being transferred
  * Large numbers of these could hurt the frame rate
* Added a "Match Any" / "Match All" button to the module GUI - https://github.com/desht/ModularRouters/issues/56
  * "Match Any" acts as previously and is the default - used in the vast majority of cases
  * "Match All" requires *all* items in the filter to match - useful e.g. to match "enchanted leather boots"
* Added an extra Player Module option: "Main Inventory (no Hotbar)" - https://github.com/desht/ModularRouters/issues/73
  * This will completely ignore the player's hotbar slots when transferring items to/from the main inventory
* Fixed the Extruder Mk2 Module just breaking everything its path when extending - https://github.com/desht/ModularRouters/issues/74
  * It's supposed to stop at (non-replaceable) blocks, just like the Extruder Mk1
* Fixed NPE triggered by other mods passing a null world/pos to certain Block methods they shouldn't - https://github.com/desht/ModularRouters/issues/72
* Fixed the "show module info" keybind (bound to "I" by default) not working in Router and Module GUI's (worked OK in default inventory screen)

### 6.0.1 (28 Apr 2020)

* Changed the way Breaker Module and Extruder Module Mk1 harvest levels are determined
  * Instead of just being able to harvest anything, the pickaxe used in crafting a Breaker Module now determines its harvest level. E.g. if you want to break Obsidian, use a Diamond Pickaxe to craft the module.
  * Breaker Module can now be crafted with *any* pickaxe, included modded pickaxes.
  * Module enchanting by crafting the module with an enchanted book is gone; instead, any enchantments on the pickaxe will be "absorbed" into the crafted module.  Silk Touch and Fortune are the useful enchantments to have.
  * Extruder Mk1 will inherit the harvest level of the Breaker Module used to craft it.
  * You can disable harvest level limits in config: `breakerHarvestLevelLimit` in the "Modules" section.
* Vacuum Module (XP Mode) now supports PneumaticCraft: Repressurized Memory Essence XP Fluid
  * No, PNC:R doesn't yet have a release for 1.15.2, but it's very close...
* Activator Module now respects its filter items
* Fixed some culling issues when router/template frame blocks are camouflaged

### 6.0.0 (26 Mar 2020)

* Initial 1.15.2 port!
* Added a Patchouli guidebook
* Added a Fluid Mk2 module, which can transfer fluids to/from tanks and blocks 12-24 blocks away
* Retextured the Item Router a bit
* Activator module, when in "use item on entity" mode, can now target multiple nearby entities 
  * choose between nearest, round robin, or random
* Add hard item limits on the number of upgrades/augments which can be added to routers/modules
  * e.g. it's no longer possible to put more than 9 Speed Upgrades in an Item Router, or more than 1 XP Vacuum Augment in a Vacuum Module

## Minecraft 1.14.4

### 5.1.4 (1 Feb 2020)

* Fixed item dupe bug

### 5.1.2 (30 Nov 2019)

* Server-side config is now common config.  If you made any changes to `saves/<world>/serverconfig/modularrouters-server.toml` you will need to modify `config/modularrouters-common.toml` after this update.  Sorry!
* Fixed NPE with Sender Mk2/Sender Mk3/Distributor when inventory was invalid or module not configured
* Fluid Module filter now supports fluid tags.
* Some other minor fluid handling improvements & fixes.
* Better initial placement rotation for rotatable blocks by the Extruder.
* Tooltips referring to "Ctrl" now use "Cmd" on Mac.

### 5.1.1 (30 Oct 2019)

* Fixed matching by item tag not always working.
* Fixed Fluid Module filter accepting empty fluid containers (only container with fluids should be accepted)
  * Fluid Module item tooltip now shows names fluids in filter rather than the container item name.
* Fixed Fluid Module being able to pick up flowing fluids (e.g. water flowing past a router) as if it were a source block.
* A fair bit of internal code cleanup.

### 5.1.0 (22 Oct 2019)

* First update to 1.14.4!
* Fluid module is now functional, although only tested with vanilla water and lava buckets.
* Camouflage Upgrade and Extruder Mk2 are now fully functional (camo works!)
* Updated some recipes to be more tag-friendly.
* Many recipes are now a little cheaper:
  * Puller Mk2 and Sender Mk2 now just need an Ender Pearl instead of Eye of Ender.
  * Added Puller/Sender Mk2 recipes which take 4 of the Mk1 module and one Ender Pearl, and give 4 of the Mk2 module.
  * Speed Upgrades need a bit more gold, but one craft now gives 3 upgrades instead of 1.
  * Upgrade/Downgrade Augment recipes now give 4 augments instead of 1.
  * Added an alternative Sender Mk1 recipe which uses a Piston instead of Bow & Arrow.
  * Muffler Upgrade recipe now gives 4 upgrades instead of 1.
* Fixed mouse-over crash for modules from 5.0.0 (https://github.com/desht/ModularRouters/issues/57)
* Redid all module, upgrade, and augment textures.
* Completely removed all java.awt.* code (good news for Mac users)

## Minecraft 1.14.2

### 5.0.0 (15 Jun 2019)

* Initial 1.14.2 port!
* Tested on SSP and dedicated server SMP.  Still early days for Forge so expect instability and bugs.
* Particle beams for Sender Mk1/2/3 and Puller Mk2 have been rewritten and now render the items in transit
* Fluid Module is NOT yet functional due to lack of Forge 1.14.2 fluid support (still in development)
* Camouflage Upgrade doesn't work yet (again waiting on some support from Forge)
* Extruder Mk2 extruded blocks don't work yet (for same reason Camo Upgrade doesn't work)
* Item filtering is slightly different: OreDict and Item Meta are no longer a thing in 1.14.2
* Those filter settings in the module GUI have been replaced by Tag matching and Item Damage matching, respectively
* Otherwise this should be functionally equivalent to the most recent Modular Routers on 1.12.2

## Minecraft 1.12.2

### 3.2.1 (1 Oct 2018)

* Camouflage: connected textures are now supported for router camo and Extruder Mk2 blocks!
* Added checkbox to Fluid Module: control whether fluid should be poured out of the router when there is already a fluid of the same type present. https://github.com/desht/ModularRouters/issues/49
* Fixed NPE when inserting an unconfigure Puller Mk2 module into a router. https://github.com/desht/ModularRouters/issues/46
* Forge build 2705 or newer required

### 3.2.0 (23 Aug 2018)

* Added the Distributor Module, a way to distribute items across multiple target inventories.  Can do round-robin, random, nearest-first or furthest-first.
* Extruder Mk2 Module is now cheaper to craft (just needs an Extruder Mk1 and a chest).
* Range Up & Range Down augments can now be crafted into each other.
* Fix dupe bug with Augments when a stack of more than one module is configured at once
* Fix last module in router slot (slot 9) not being configurable
* Fix possible client disconnect in conjunction with TheOneProbe

### 3.1.5 (30 May 2018)

* Fix item dupe with Dropper/Flinger in some circumstances; cross-mod incompatibility with Realistic Item Drops and its "Dupe Workaround" disabled, which is the case in Sevtech Ages at this time (https://github.com/DarkPacks/SevTech-Ages/issues/2649)

### 3.1.4 (28 Apr 2018)

* Blocks extruded by Extruder Mk1/2 now push entities along the direction of extrusion.
* Added Pushing Augment for Extruder Mk1/2 to increase the push force of extruding blocks.  Pushing Augments can stack, and large numbers of augments can push entities a long way...
* Fixed Activator Module behaviour when clicking items into/out of holder blocks, e.g. Astral Sorcery Starlight Infuser, or Tinker's Construct Casting Basin.
* Activator Module entity mode now works on all entities, not just living entities.
* Improved rendering for certain blocks extruded by Extruder Mk2 (in particular blocks which connect to each other, like iron bars or glass panes)
* Now using a mod-local tick counter for router synchronisation instead of world tick time; should resolve router sync issues when used in conjunction with mods which modify day length or otherwise tweak server times.

### 3.1.3 (25 Mar 2018)

* Fixed crash when running with TheOneProbe 1.4.21+
* Added the Mimic Augment, to be added to the Extruder Mk2 module.  With this augment, extruded fake blocks also mimic block hardness, blast resistance, redstone emission and light emission of the mimicked block.
* Routers now ignore redstone signals from sides currently extruding blocks (from Extruder Mk1 or Extruder Mk2) - e.g. extruding a redstone block will no longer lock the router and prevent the extruded block from being retracted again.

### 3.1.2 (26 Jan 2018)

* Fixed Vacuum Module GUI crashes (https://github.com/desht/ModularRouters/issues/32)
* Vacuum Module XP mode: Actually Additions Solidified Experience is now an option when choosing XP type
* Vacuum Module XP mode: it's now possible to auto-eject XP fluids to a tank adjacent to the router; this may be useful if you don't have a mod which provides a tank that can be filled while in item form in the router's buffer

### 3.1.1 (11 Jan 2018)

* Vacuum Module XP augment enhancements: more XP fluids are now supported (Cyclic, Industrial Foregoing, Thermal Expansion), and the Vacuum Module GUI now allows selection of the desired XP fluid type (as well as vanilla Bottles o' Enchanting).
* Some tooltip formatting improvements (wrapped some over-long lines of text)

### 3.1.0 (9 Dec 2017)

* Added the Activator Module which allows simulates right-clicking blocks or entities with the item in the router's buffer.
* The Extruder Mk2 can now have non-block items placed in its template; these act as spacers, "placing" an air block when extruding.
* Module GUI has a new help button in the top-right corner; clicking this toggles mouse-over popup help text in the module GUI.
* When held, targetable modules (Sender Mk2/3, Puller Mk2) now highlight the targeted inventory
* Better placement behaviour for horizontally-rotated blocks, e.g. stairs - they will now face the router when placed by Placer or Extruder modules (in the same way they would face a player) https://github.com/desht/ModularRouters/issues/29
* Chisel blocks are now properly rendered when used as camouflage or in the Extruder Mk2 (this fix was in the 1.11.2 version for ages but got overlooked for 1.12.2)
* Known issue: connected Chisel textures don't work with camouflaged or Extruder Mk2 extruded blocks
* Fixed a potential Puller module deadlock when pulling from inventories where not all slots can be extracted from.  https://github.com/desht/ModularRouters/issues/28

### 3.0.3 (10 Sep 2017)

* Fixed possible server crash when placing blocks with Placer or Extruder Mk1 modules.  https://github.com/desht/ModularRouters/issues/25

### 3.0.2 (28 Aug 2017)

* Fixed possible client crash with Extruder module (https://github.com/desht/ModularRouters/issues/24)

### 3.0.1 (11 Aug 2017)

* Fixed item dupe issue with shulker boxes (https://github.com/desht/ModularRouters/issues/23)
* Guidebook (GuideAPI) is now working again.  GuideAPI 2.1.4-56 or later needed.

### 3.0.0 (4 Jul 2017)

* This release is for Minecraft 1.12, and requires Forge 14.21.1.2387 or later. Significant internal rewrite to support 1.12 block/item/recipe registration changes. This version will *not* work well with worlds from older releases of the mod, sorry.
* Re-did module & upgrade item textures, to be hopefully cleaner & easier to distinguish.
* Module enhancement recipes are gone, replaced instead by module augment items which can be inserted/removed via the module GUI.
* Range upgrades & downgrades are removed; replaced by Range Up/Down Augments.
* The old deprecated Sorter and Mod Sorter modules are gone completely. Use a Bulk Item Filter in any module instead of a Sorter; use a Mod Filter in any module instead of a Mod Sorter.
* Modules can have Stack Augments, which override any Stack Upgrades in the router (so a router can now have several modules each processing a different number of items per tick)
* Module GUI backgrounds are now tinted the same colour as the respective module item; if you don't like this, set "backgroundTint" to false in the mod config.
* Module & filter settings are now always shown in the item tooltip (don't need to hold Shift); if you prefer the old behaviour, set "alwaysShowSettings" to false in the mod config.
* Dropped IInventory support completely; there's no excuse for mods not supporting capabilities by now.
* CoFH RF API is no longer bundled, but the Redstone Flux mod is detected for and supported. Tesla and Forge Energy are also still supported.
* Camouflage and Extruder improvements and fixes from v1.4.1 and v2.2.1 are also included.
* The GuideAPI guidebook is currently not functional, but should added back in the near future (working out some compatibility issues with GuideAPI and the new Forge item registration system).

## Minecraft 1.11.2

### 2.2.2 (28 Aug 2017)

* Fix possible client crash with Extruder module (https://github.com/desht/ModularRouters/issues/24)
* Fix item dupe issue with shulker boxes (https://github.com/desht/ModularRouters/issues/23)

### 2.2.1 (29 Jun 2017)

* Camouflage upgrade now properly mimics the colour of tintable blocks (in particular grass blocks no longer appear grey)
* Camouflage upgrade and Extruder Mk2 template blocks can now mimic Chisel blocks (and any blocks which render in a layer other than CUTOUT_MIPPED) - https://github.com/desht/ModularRouters/issues/19
* Fixed bug where Inspection Filter could not match conditions where the target value was zero (e.g. "enchantment = 0" or "enchantment < 1") - https://github.com/desht/ModularRouters/issues/20

### 2.2.0 (12 Jun 2017)

* Added XP enhancement to Vacuum Module. With XP enhancement added, the Vacuum Module will absorb XP orbs instead of items. Orbs are converted to EnderIO XP juice if EnderIO is installed and the router holds a fluid container item (e.g. EnderIO tank); otherwise orbs are converted to Bottles o' Enchanting (1 bottle per 7 xp absorbed).
* Made several recipes oredict-aware
* Routers in item form now show a list of the installed modules (if any) in the item tooltip, not just the number of modules.
* Stack Upgrade item tooltip now shows the right items-per-tick value
* Block placement tweaks: router will try to place rotatable blocks (including other item routers...) oriented according to the facing of the Placer Module. Placed blocks will face toward the router where possible (as if being placed by a player).
* Fixed server crash if Dropper/Flinger modules encountered a buggy 0-sized itemstack (shouldn't happen, but bugs in other mods can cause it).
* Fixed bug where a brand new Bulk Item Filter placed in a module in a router would not remember its filter contents after the GUI was closed.
* Fixed NPE when placing items in Mod Filter GUI.

### 2.1.1 (13 Mar 2017)

* Added the Range Downgrade, which reduces a router's range by one block per "upgrade".  This will likely be most useful for limiting the range of the Vacuum Module.  A Range Upgrade can be crafted directly into a Range Downgrade, and vice versa.
* Fix to infinite water source detection by the fluid module.
* Fix to filtering fluids by the fluid module when pulling fluids in from fluid blocks in the world.
* Fix to the regex filter where it did not always properly match on the displayed item name (wrongly using the internal unlocalised name instead).
* The Extruder Mk2 recipe now uses oredicted chests ("chestWood") so chests other than vanilla oak chests should be accepted.

### 2.1.0 (1 Mar 2017)

* Added the Extruder Mk2 module, which can place blocks from a configurable pattern, allowing for multiple types of block to be extruded.  These blocks are "virtual" in that they're created out of nowhere, but also drop nothing if broken, and can't be crafted normally.
* The Inspection Filter can now inspect food values of items (the value of food items is the number of half-shanks restored, and the value of non-food items is 0).
* Modules can now be crafted by themselves to reset all data (filter, blacklist, enhancements...) back to the newly-crafted state.  Note that any enhancements will also be lost!
* Targetable modules (Sender Mk2/3, Puller Mk2) can now be sneak-right-clicked on a non-inventory block (or air) to clear their target.
* Fixed NPE when left-clicking the regulation icon in the GUI of a module with the regulation enhancement enabled.
* Puller Mk1/Mk2 can now pull properly from locked storage drawers (previously any locked drawer with an empty slot would cause the puller modules to get confused and stop pulling).

### 2.0.2 (6 Feb 2017)

* Feature: Extruder Module can now place cocoa beans if extruding alongside jungle logs (either horizontally or vertically); cocoa bean farming is now possible.
* Fix: broken texture for Blast Upgrade
* Fix: when breaking a router with items in the buffer, not all items were always getting dropped

### 2.0.1 (18 Jan 2017)

* Routers can now place skulls
* Added Puller Mk2 module to pull at a distance (up to 12 blocks, or 24 with range upgrades)
* Added Blast Upgrade to make router immune to explosion damage and boss destruction
* Increased default range of Sender Mk2 module (base: 16 -> 24, max 32 -> 48)
* Added zh_CN translation
* Couple of minor bug fixes

### 2.0.0 (3 Jan 2017)
*Archived*

* Pretty much the same as v1.2.0 (for MC-1.10.2), but runs on MC-1.11.2
* One new feature: Placer and Extruder modules can now plant crops (modded crops should in general work too - tested successfully with Actually Additions crops)

## Minecraft 1.10.2

### 1.4.2 (28 Aug 2017)

* Fix possible client crash with Extruder module (https://github.com/desht/ModularRouters/issues/24)

### 1.4.1 (29 Jun 2017)

* Camouflage upgrade now properly mimics the colour of tintable blocks (in particular grass blocks no longer appear grey)
* Camouflage upgrade and Extruder Mk2 template blocks can now mimic Chisel blocks (and any blocks which render in a layer other than CUTOUT_MIPPED) - https://github.com/desht/ModularRouters/issues/19
* Fixed bug where Inspection Filter could not match conditions where the target value was zero (e.g. "enchantment = 0" or "enchantment < 1") - https://github.com/desht/ModularRouters/issues/20

### 1.4.0 (12 Jun 2017)

* Added XP enhancement to Vacuum Module. With XP enhancement added, the Vacuum Module will absorb XP orbs instead of items. Orbs are converted to EnderIO XP juice if EnderIO is installed and the router holds a fluid container item (e.g. EnderIO tank); otherwise orbs are converted to Bottles o' Enchanting (1 bottle per 7 xp absorbed).
* Made several recipes oredict-aware
* Routers in item form now show a list of the installed modules (if any) in the item tooltip, not just the number of modules.
* Stack Upgrade item tooltip now shows the right items-per-tick value
* Block placement tweaks: router will try to place rotatable blocks (including other item routers...) oriented according to the facing of the Placer Module. Placed blocks will face toward the router where possible (as if being placed by a player).
* Fixed server crash if Dropper/Flinger modules encountered a buggy 0-sized itemstack (shouldn't happen, but bugs in other mods can cause it).
* Fixed bug where a brand new Bulk Item Filter placed in a module in a router would not remember its filter contents after the GUI was closed.

### 1.3.1 (13 Mar 2017)

* Added the Range Downgrade, which reduces a router's range by one block per "upgrade".  This will likely be most useful for limiting the range of the Vacuum Module.  A Range Upgrade can be crafted directly into a Range Downgrade, and vice versa.
* Fix to infinite water source detection by the fluid module.
* Fix to filtering fluids by the fluid module when pulling fluids in from fluid blocks in the world.
* Fix to the regex filter where it did not always properly match on the displayed item name (wrongly using the internal unlocalised name instead).
* The Extruder Mk2 recipe now uses oredicted chests ("chestWood") so chests other than vanilla oak chests should be accepted.

### 1.3.0 (1 Mar 2017) 

* Added the Extruder Mk2 module, which can place blocks from a configurable pattern, allowing for multiple types of block to be extruded.  These blocks are "virtual" in that they're created out of nowhere, but also drop nothing if broken, and can't be crafted normally.
* The Inspection Filter can now inspect food values of items (the value of food items is the number of half-shanks restored, and the value of non-food items is 0).
* Modules can now be crafted by themselves to reset all data (filter, blacklist, enhancements...) back to the newly-crafted state.  Note that any enhancements will also be lost!
* Targetable modules (Sender Mk2/3, Puller Mk2) can now be sneak-right-clicked on a non-inventory block (or air) to clear their target.
* Fixed NPE when left-clicking the regulation icon in the GUI of a module with the regulation enhancement enabled.
* Puller Mk1/Mk2 can now pull properly from locked storage drawers (previously any locked drawer with an empty slot would cause the puller modules to get confused and stop pulling).

### 1.2.5 (6 Feb 2017)

* Feature: Extruder Module can now place cocoa beans if router is placed alongside jungle logs, either horizontally or vertically.

### 1.2.4 (23 Jan 2017)

* Placer, Extruder & Breaker modules now have support for more block types, in particular Chisels & Bits blocks (note that asymmetrically-chiseled blocks don't always appear to place/break facing the same way - I'm looking for ways to better handle that).
* Fixed potential infinite recursion bug for routers in pulsed mode when placing/breaking blocks.
* Added zh_CN translation.

### 1.2.3 (12 Jan 2017)

* Added the Puller Mk2 module - pull items from inventories within 12 blocks (24 with range upgrades).  Can be adjusted in config/modularrouters.cfg if desired.
* Colour for sender & puller particle beams changed: sender mk1/2 beam is orange, sender mk3 is magenta, puller beam is blue.
* Removed spurious debug messages (pertaining to explosions) mistakenly left in the 1.2.2 release.
* Increased the default range of Sender Mk2 from 16 to 24 blocks (max range 32 -> 48).  Can be adjusted in config/modularrouters.cfg if desired.

### 1.2.2 (9 Jan 2017)
*Archived*

* Extruder module no longer stops in confusion if it encounters an empty (or liquid) block while retracting; it will now just skip over gaps.
* Placer module can now place down skulls, correctly positioned on blocks like soul sand...
* Added the Blast Upgrade, giving the router immunity from explosion damage and block-breaking by certain entities...

### 1.2.1 (4 Jan 2017)
*Archived*

* Placer and Extruder modules can now plant crops (backport of feature from v2.0.0 release for MC-1.11.2)
* This release is basically feature-equivalent to the v2.0.0 release now
* Couple of other minor fixes

### 1.2.0 (19 Dec 2016)
*Archived*

* Added the Fluid Module: routers can use this to transfer fluids to/from a fluid container item in the buffer (e.g. vanilla buckets and tanks from other mods). Fluid Module can pull or send to fluid blocks in the world, or in tanks from other mods. The Regulator enhancement works with Fluid Modules, but uses a percentage of the tank's capacity rather than number of items.
* Also added Fluid Transfer Upgrade to increase a router's rate of fluid transfer (which is 50mB/tick by default).
* Fluid pipes from other mods will now connect to an item router if it has a fluid container item in the buffer; this allows pipes to pump fluids into & out of that container, without needing a Fluid Module. The Fluid Module is needed, however, for the router to actively push or pull fluids.
* Energy cables from other mods will now connect to an item router if it has an energy container item in the buffer (e.g. Actually Additions batteries, EnderIO capacitor banks...). Support for Forge Energy, Tesla and CoFH RF is included, but routers do not implement the CoFH RF API; RF-holding items are exposed to other mods via the Forge Energy capability.  Note that not all energy-containing items actually work (Mekanism energy tablets do not, and some other modded energy storage blocks can't be manipulated in item form).
* Bulk Item Filter improvements: 1) Filter now has a normal inventory making it easier to add/remove items directly from your inventory, 2) Filter now also works with NBT and Oredict filtering.
* Added the Inspection Filter which can filter on miscellaneous conditions, including enchantment levels, durability percentage, and fluid/energy percentage in a container item. More inspection conditions are likely to be added in future.
* Added the Muffler Upgrade to reduce the sounds and particle effects made by certain modules. More upgrades progressively reduce sounds/effects (see item usage tooltip). Has no effect if sounds/effects are disabled in config, of course.
* Routers now have 5 upgrade slots instead of 4.
* Added Pickup Delay enhancement for Dropper & Flinger Modules: possible to enforce a delay before items dropped by these modules can be picked up.
* Added Fast Pickup enhancement for Vacuum Module: ignore any pickup delay on a dropped item.
* JEI support for enhancement recipes to better document the nature of the enhancement being added.
* Breaker Module will now break items even if the router's buffer is full (broken items will be dropped on the ground, of course)
* Breaker Module improvement: filtering now applies to actual drops from the broken block, not just the block's default drops. E.g. if you filter on an empty tank, the breaker will now only break the tank when it's actually empty.
* Placer Module improvement: the placer will now attempt to restore NBT from any item it places. E.g. the fluid level of a tank, or the energy level on any energy storage block...

### 1.1.3 (19 Nov 2016)

* CRITICAL RELEASE: this release fixes a couple of item duping exploits introduced in 1.1.0.  If you are using 1.1.0, 1.1.1 or 1.1.2, UPDATE NOW.  1.0.x releases do not have the exploits.

### 1.1.2 (17 Nov 2016)
*Archived*

* Fixed problem with pressure plates not being placeable on top of item routers (this worked in 1.0.x, camouflage features in 1.1.0 broke it)
* Known issue: pressure plates don't pop off the top of a router if you camouflage it as e.g. a slab.  No simple fix for this right now, sorry.  Other attachables (e.g. buttons, levers) do behave correctly.  It's down to how attachable blocks check for solidity in differing ways (pressure plates do it badly!)
* Minor rendering performance improvement (rendering faces are now culled properly, based on whether the router is camouflaged or not).
* Fixed bug where router upgrades did not always get properly saved when a router is broken.

### 1.1.1 (10 Nov 2016)
*Archived*
 
* Added the Sync Upgrade, which can be used to make routers run at the same time.  https://github.com/desht/ModularRouters/wiki/Upgrades#sync-upgrade
* Made the Blank Upgrade recipe a bit cheaper (now uses 1 Lapis instead of 1 Diamond)
* Made the Speed Upgrade recipe a bit more expensive
* A bit of GUI polishing in several places

### 1.1.0 (7 Nov 2016)
*Archived*

* Modules can now be enhanced to have invidual redstone modes - https://github.com/desht/ModularRouters/wiki/Module-Enhancements#redstone-enhancement
* Modules can now be enhanced to regulate items - keep a certain number of in an inventory - https://github.com/desht/ModularRouters/wiki/Module-Enhancements#regulator-enhancement
* Added Extruder Module to extend/retract a row of blocks (bit like the Tinker's Drawbridge from 1.7.10) - https://github.com/desht/ModularRouters/wiki/Modules#extruder-module
* Added Camouflage Upgrade to disguise the router as another block (goes well with the Extruder Module) - https://github.com/desht/ModularRouters/wiki/Upgrades#camouflage-upgrade
* Add "Smart Filters" - craftable filter items which go into a module's filter as usual but do more sophisticated item matching - https://github.com/desht/ModularRouters/wiki/Smart-Filters
* Bulk Item Filter: high-performance matching of up to 54 items per filter
* Mod Filter: match against up to 6 mods per filter
* Regex Filter: match item display name against regular expressions
* You can now configure installed modules (and filters) by middle-clicking them in the GUI.  Pressing 'c' (or whatever you've set in config) still works, too.
* Deprecated the Sorter Module in favour of the Bulk Item Filter (Sorter Modules still work but can't be crafted anymore - you can craft a Sorter Module into a Bulk Item Filter)
* Deprecated the Mod Sorter Module in favour of the Mod Filter (Mod Sorter Modules still work but can't be crafted anymore - you can craft a Mod Sorter Module into a Mod Filter) Flinger module now has a sound & smoke effect when it flings items (can be disabled in config)
* Lots of other minor bugfixes, polishing, performance improvements... see https://github.com/desht/ModularRouters/commits/master for full details.

### 1.0.3 (16 Sep 2016)
*Archived*

* Fixed player's hand being rendered partially transparent: https://github.com/desht/ModularRouters/issues/3
* Added "Router Eco Mode": allow routers to slow down if idle for a certain amount of time; intended to save CPU on busy servers.  See https://github.com/desht/ModularRouters/wiki#eco-mode for more info.
* The Player Module can now also interact with a player's (vanilla) ender inventory.  Allows auto-inserting/extracting to/from vanilla ender chests.

### 1.0.2 (5 Sep 2016)
*Archived*

* Fixed startup crash if Waila isn't installed

### 1.0.1 (5 Sep 2016)
*Archived*

* Fixed crash on startup if TheOneProbe isn't installed

### 1.0.0 (5 Sep 2016)
*Archived*

* Initial release
