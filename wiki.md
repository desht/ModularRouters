# ModularRouters Wiki

## Overview

Welcome to Modular Routers!  This is a low-tech, but (hopefully) situationally very useful mod for moving items around in various ways.  With a single block - the Item Router - and one or more plug-in modules, you can pull items from an inventory (including modded inventories like Storage Drawers), send them to other inventories near & far, place items as blocks, break blocks, drop items into the world, and more.  Think of the Item Router like a super-configurable Hopper on steroids...

## The Item Router

![Item Router will Puller and Dropper modules installed](https://github.com/desht/ModularRouters/blob/master/images/router_gui.png)

The Item Router is the centre of the system: it has a single buffer slot for items, nine module slots, and four upgrade slots.  You can right-click an Item Router to open its GUI.

The button in the top right of the GUI allows redstone behaviour to be configured, and has a tooltip indicating the mode:

* **Always Run** (default) - router will run regardless of whether or not it has a redstone signal
* **High** - router will only run if it has a redstone signal (any strength > 0)
* **Low** - router will only run if it has _no_ redstone signal
* **Never Run** - router will not run at all
* **Run Once on Pulse** - router will run _once_ on a rising redstone edge (i.e. whenever the signal strength increases).  This mode could be used to synchronise the router to a timer, for example.  Note: the router will still not be allowed to run any faster than usual - once per second without Speed Upgrades.

Click the button to cycle to the next mode, or shift-click to cycle to the previous mode.

A comparator placed against a router will measure the number of items in the buffer as a proportion of the maximum stack size, following usual vanilla comparator rules.

### The Buffer

The Item Router has a single-slot buffer for items.  Why only a single slot?  The router is intended to move items around, not store them.  However, if you want to use it as a 1-slot chest, you can; its inventory is fully accessible by vanilla hoppers and any other mods piping systems.

But to properly use a router, you need...

## Modules

Modules actually define what a router does.  An Item Router can have up to nine modules installed, and every router tick (which is 20 server ticks or 1 second by default, but see [Speed Upgrades](https://github.com/desht/ModularRouters/wiki#-speed-upgrade)), it executes each installed module in order, from the leftmost to the rightmost.

There are 13 different module types, each of which operates on the router's buffer in a specific way.  All modules have a common configuration interface, which can be accessed by right-clicking with the module in hand, or by pressing the 'C' key (configurable) while the mouse is over an installed module in the router's GUI.

By default, a module operates on a single item at a time, but this can be increased with one or more [Stack Upgrades](https://github.com/desht/ModularRouters/wiki#-stack-upgrade).

![](https://github.com/desht/ModularRouters/blob/master/images/module_gui.png)

1. The filter.  All modules have a 9-slot filter interface where you can add ghost items.  The module will _only_ operate on items which match items in the filter.

2. Whitelist/Blacklist toggle.  Default is an empty Blacklist which means "allow everything".  An empty Whitelist means "deny everything".  

3. Match/Ignore Metadata.  This allows matching or ignoring of items' damage values; either the damage on a item with durability, or the metadata on items such as wool.  Example: if you select Ignore Metadata and put wool in the filter, the module will work on wool of _any_ colour.

4. Match/Ignore NBT.  This allows matching or ignoring of items' extended NBT data, e.g. enchantment data, or mod-specific extended data.

5. Use/Ignore Oredict.  This allows for oredict-based matching.  Vanilla example: all music discs have an Oredict equivalence.  If you enable oredict matching and put any music disc in the filter, _all_ music discs will be matched by this module.

6. Termination.  If Termination is **ON**, and this module succeeds in processing an item, no more modules will be executed on this router tick.  Example: say the first module in the router is set to match Stone, and the second module is set to match anything - if there's a stack of Stone in the router, and you want it all to be processed by the first module (e.g. send it all in a certain direction), you would enable Termination on the first module, so as soon as the router finds a match, it stops there.

7. Direction.  Most modules need a direction to operate in; e.g. to know where to send an item, which block to break, where to place a block...  The default direction is "None", and in most cases a module with a direction of None will not do anything, so you will need to explicitly configure a direction here.  Directions are always relative to the router's facing (except that a router only faces horizontally so Up and Down are always absolute).

8. Extras. Some modules may have some extra controls for configurable certain module-specific settings. E.g. in this example, the Detector Module has controls for setting the redstone signal level and whether to emit a strong or weak signal.  This section will be blank for modules which don't have any module-specific settings.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/breakerModule.png) Breaker Module

This module tries to break a block, adjacent to the router in the module's configured direction.  Most blocks are breakable, although anything with an unbreakable hardness (e.g. vanilla Bedrock or End Portal) can not be broken, nor can any fluid blocks.  If the block is broken, its primary drop(s) will be pulled into the item router's buffer.

* If there's already something else in the buffer, or the buffer is full, the block will _not_ be broken.
* If breaking the block yields more than one type of drop (e.g. mature wheat drops both wheat and seeds), only the first drop will be pulled into the buffer and other items will be left as items on the ground.  In this case, you might also want a Vacuum Module to pull in the extra drops.  If there are multiple drops of the same type (e.g. redstone ore dropping several redstone dust), all stackable drops will be pulled into the buffer where possible.
* The Breaker Module may be crafted with either a Fortune or Silk Touch enchanted book to gain those enchantments' abilities.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/detectorModule.png) Detector Module

This module doesn't actually manipulate items, but instead detects specific items in the router's buffer.  If the buffer contents are matched by the module's filter, it will make the router emit a redstone signal in the configured direction (or all directions if the configured direction is NONE).  The signal level (default: 15) and signal type (default: weak) can be adjust via the module GUI.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/dropperModule.png) Dropper Module

This module tries to drop an item from the router's buffer as an item entity in the world.  The drop will be adjacent to the router in the module's configured direction.  The item entity will be placed with a zero velocity, unlike a vanilla Dropper.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/flingerModule.png) Flinger Module

This module behaves like the dropper module, except that it also imparts a configurable velocity to the dropped item, potentially throwing the item a considerable distance.  You can adjust the item's speed, pitch and yaw via the module's GUI.

* Pitch and Yaw are in degrees, relative to a *base* pitch or yaw.
* If the module ejects Up or Down, the base pitch is +90° or -90°, respectively.  Otherwise, the base pitch is 0°.
* If the module ejects Up or Down, the base yaw is the router's facing direction.  Otherwise the base yaw is taken from the module's direction.
* Example: a speed of 1.0, pitch/yaw of 0°, and a horizontal module direction, will throw an item along the ground for a distance of about 7 blocks.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/modsorterModule.png) Mod Sorter Module

This module will try to place an item from the router's buffer into the adjacent inventory in the module's configured direction.  However, it will _only_ place an item into the inventory if there is an item in the inventory from the same mod as the buffer item.  Note: this is in addition to any filter you may have defined on the module.

See also [Sorter Module](https://github.com/desht/ModularRouters/wiki#-sorter-module)

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/placerModule.png) Placer Module

This module tries to place an item from the router's buffer as a block, adjacent to the router in the module's configured direction.

* If the item isn't a block or the destination block is obstructed, nothing will be done.
* Normal Minecraft placement rules are followed; e.g. sugar cane can only be placed next to water on sand/dirt.
* Fluid blocks will be replaced, as will replaceable blocks such as tall grass.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/pullerModule.png) Puller Module

This module tries to pull the first eligible item from the inventory in its configured direction, into the router's buffer.  If the buffer is full or contains something else, nothing will be pulled.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/sender1Module.png) Sender Module Mk1

This module tries to send an item from the router's buffer to an inventory in the module's configured direction:

* The target inventory can be up to 8 blocks away (up to 16 with [Range Upgrades](https://github.com/desht/ModularRouters/wiki#-range-upgrade) )
* The target inventory must be directly along the X, Y, or Z axis
* The router must have clear line of sight to the target inventory; no opaque blocks, but blocks such as glass, fences, iron bars etc. are OK

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/sender2Module.png) Sender Module Mk2

This more powerful sender module tries to send an item from the router's buffer to a nearby inventory:

* The target inventory can be up to 16 blocks away (up to 32 with [Range Upgrades](https://github.com/desht/ModularRouters/wiki#-range-upgrade) )
* Clear line of sight is _not_ required
* To select the target inventory, Shift-Right-click the target with the Sender Mk2 module in hand.  You will get a confirmation message.
* If you need to check where a Sender Mk2 is configured to, Left-click with the module in hand, and a particle effect will stream toward the target inventory (the co-ordinates of the target are also shown in the module item's tooltip).
* The Direction setting is ignored by this module.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/sender3Module.png) Sender Module Mk3

This top-end sender module operates very much like the Mk2 Sender, but can send to _any_ inventory (in any dimension) with no restrictions!  Note: the target inventory must be chunk-loaded; Modular Routers will not do this for you.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/sorterModule.png) Sorter Module

This module will try to place an item from the router's buffer into the adjacent inventory in the module's configured direction.  However, it will _only_ place an item into the inventory if there is already a matching item in the inventory.  Note: this is in addition to any filter you may have defined on the module.

This can be used to make a very simple early-game sorting system: consider a horizontal row of item routers with chests on top of them.  Each router should have two modules: firstly a Sorter Module sending UP, and secondly a Sender Module Mk1 sending RIGHT (to the next router).  The sorter module should have Termination ON (unless your routers all have enough Stack Upgrade to process a full stack at once, which is unlikely early-game) to avoid items being wrongly sent along by the Sender Module.  The final router can have a single Sender Mk1 sending UP, to a catch-all chest.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/vacuumModule.png) Vacuum Module

This module scans for dropped items in a 13x13x13 cubic area around the router (i.e. up to 6 blocks in each direction), and absorbs them into the router's buffer, if possible.

* The range can be increased up to 25x25x25 (12 blocks in each direction) with [Range Upgrades](https://github.com/desht/ModularRouters/wiki#-range-upgrade).  Each Range Upgrade increases the distance by 1 block, so up to 6 Range Upgrades can be usefully installed.
* The scanned area is centred on the router if the module's direction is "None".  If the module has an actual direction configured, the area is offset in that direction by 6 blocks (plus one for each Range Upgrade installed).  E.g. with a direction of UP, the module will only scan an area directly above the router.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/voidModule.png) Void Module

This dangerous module permanently destroys items in the router's buffer!  It is strongly recommended to configure this module with a Whitelist to prevent accidental deletion of valuable items...

## Upgrades

While modules define what a router does, Upgrades affect how the router does it.  There are three different upgrade types:

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/stackUpgrade.png) Stack Upgrade

By default, a router operates on a single item at a time, even if there is more than one item in the buffer.  For example, a Puller module will pull one item from an adjacent inventory, even if there's a whole stack available in that inventory.

By adding Stack Upgrades to a router, this can be increased.  Each Stack Upgrade doubles the number of items that can be processed, up to a maximum of 64, or the item's native stack size (e.g. 16 for Ender Pearls).  It therefore follows that 6 is the maximum number of useful Stack Upgrades which can be installed in one router.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/speedUpgrade.png) Speed Upgrade

By default, a router ticks every 20 server ticks, or once per second.  On each tick, every installed module is executed in order, left to right.

If you need a router to operate faster than this, you can add Speed Upgrades; each Speed Upgrade reduces the tick interval by 2 server ticks, down to a minimum of every 2 server ticks (or 10 times per second).  The base tick rate, tick increase per upgrade, and hard minimum tick rate are all configurable in the module's config.

Note that for performance reasons, Stack Upgrades should always be preferred over Speed Upgrades where possible; use Speed Upgrades judiciously and only where absolutely required for maximum item transfer rate.  Be Kind To Your Server (tm).

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/rangeUpgrade.png) Range Upgrade

This upgrade only affects the following modules:

* Sender Module Mk1
* Sender Module Mk2
* Vacuum Module

Each installed Range Upgrade increases the range of those modules by 1 block; see individual module documentation for more details.

### ![](https://github.com/desht/ModularRouters/blob/master/src/main/resources/assets/modularrouters/textures/items/securityUpgrade.png) Security Upgrade

This module, when inserted, will restrict the players who can open the router's GUI.  By default, it only permits the player who crafted the upgrade.

* You can add extra players to a Security Upgrade by right-clicking the player with the upgrade in your main hand.
* You can remove players from a Security Upgrade by shift-right-clicking the player.
* There is a maximum of 6 additional players per Security Upgrade (so 7 including the creator), but you can install more than one Security Upgrade in a router if necessary.
* The Security Upgrade does not prevent a router being broken by a player; but since upgrades and modules remain in a broken router, players can't steal modules/upgrades or reconfigure a router (when the router is placed back down, the Security Upgrade remains installed).  If you want to prevent a router being broken, you may wish to explore other mods' block protection capabilities.
