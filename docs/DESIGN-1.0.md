# Better Farming++ 1.0: audit, market gaps and plan

This document records what 0.5.0 actually did (read from the code and data, not the README), what players expect
from a farming quality-of-life mod today, where 0.5.0 fell short, and what 1.0 changes. Every 1.0 claim below is
implemented in all ten builds and checked in game by `tools/qa/farming_checks.py` (see
[CONTRIBUTING](../CONTRIBUTING.md#testing-in-game) and [QA-1.0.0](QA-1.0.0.md)).

## 1. Audit of 0.5.0

| Area | What the code and data did |
|------|----------------------------|
| Right-click harvest | Only blocks extending `CropBlock` (wheat, carrots, potatoes, beetroots). Nether wart and cocoa, which the harvest indicators mark as ready, could not be harvested. Only worked while holding one of the six vanilla hoes listed in the config; modded hoes needed a config edit. |
| How a crop was harvested | The mature crop's state was **copied into the block above it** (`setBlock(y + 1)`), that copy was broken, then the crop's age was reset. Whatever stood above the crop (a torch, a slab, a lantern on a chain) was **destroyed without dropping**. Drops ignored the tool (no Fortune), the hoe never lost durability, and the replant was free: no seed was taken from the drops. |
| Area harvest | None. One crop per click. |
| Farming Crate | Scanned a **6×6** square offset `-3..+2` (lopsided, not centred on the crate) on the crate's own layer, every second, with the same "copy above and break" trick. It then grabbed **every item entity** within 3 blocks (seeds, but also items players dropped), deleted the entity and inserted **one item of the stack** (`setCount(1)`): a drop of 3 seeds stored 1 and deleted 2; a full crate deleted everything it picked up. Insertion went through any face, and so did extraction. |
| Crate GUI | A fixed texture with 27 slots, no title, no status. The menu leaked internals (a 27-entry state map, unused slot fields). |
| Trampling protection | Farmland was protected only from the player **holding a whitelisted hoe** in the main hand; every other player and every mob still trampled it. |
| Harvest indicators | Vanilla crop models overridden by the mod's assets (an animated "!" above ripe wheat, carrots, potatoes, beetroots, nether wart, sweet berries and attached stems). The off switch wrapped baked models, an API that no longer exists on 26.x. Because the overrides live in the mod's own assets, a resource pack that retextures crops could only win by load order. |
| Config | `config/betterfarming/global.json` through a separate library (JaUML) that had to be installed for every loader and version; no comments, no explanation of the keys. |
| 1.21.1 data | The recipe and the loot table were in the pre-1.21 folders (`recipes/`, `loot_tables/`) with a pre-1.21 result key, so on 1.21.1 **the crate could not be crafted and dropped nothing when broken**. |
| Versions | 1.20.1 (Fabric, Forge) and 1.21.1 (Fabric, NeoForge). |
| Code | MCreator-generated procedures with repeated block lookups, unused screen/menu state plumbing, a log line-free but test-free code base. |
| Tooling | No tests, no QA scripts, no release jars in the repo; the publish workflow guessed the loader from the file name (`1.21` meant NeoForge) and fell back to hard-coded Minecraft versions. |

## 2. What players expect from a farming mod

Gathered from the comment sections, issue trackers and reviews of the popular farming, harvest and crop mods, and
from long-running community wishes ("let me harvest without breaking and replanting", "my hoe should do
something", "farmland needs a water block every 4 blocks and ruins my design", "villagers and I keep trampling the
field", "I want a farm that runs itself without a redstone contraption"). Recurring expectations:

1. **Right-click harvest that just works** on every vanilla crop, including nether wart and cocoa, and on modded
   crops through tags; replanting must not duplicate seeds, Fortune should count and tools should wear.
2. **Area harvest with better hoes**: a reason to craft a diamond hoe; sneaking to harvest a single crop.
3. **Never break anything around the crop** (decorations, lights, the block above).
4. **No trampling**, by anyone, without having to hold a specific item.
5. **Automation without redstone**: a block that harvests, replants and stores, that hoppers can empty and that
   tells you when it is full instead of wasting items.
6. **Growth and watering**: a way to water farmland without breaking up the field with water blocks, and to speed
   crops up without spamming bone meal.
7. **Visual feedback** on ripe crops, that players can turn off.
8. **No hard dependencies**, a readable config, server-side safety in multiplayer, stable ids so worlds keep
   working, and every loader on the versions people play.

## 3. Gap table

| Expectation | 0.5.0 | 1.0 |
|-------------|-------|-----|
| Crops covered by right-click | 4 (`CropBlock` only) | Every mature crop with an `age`: wheat, carrots, potatoes, beetroots, nether wart, cocoa, plus any block in `#better_farming_plus:harvestable` (modded crops extending `CropBlock` work without a tag) |
| What may harvest | 6 listed hoes | An empty hand or any item in `harvest.tools` (default `#minecraft:hoes`, so modded hoes work) |
| Replant | Free (no seed used) | One seed is taken from the drops; if a crop drops no seed, it is not replanted |
| Fortune / durability | ignored / never | Drops use the tool (Fortune counts); 1 durability per crop (configurable) |
| Block above the crop | destroyed without drops | never touched |
| Area harvest | none | **Sweep**: iron and golden hoes 3×3, diamond 5×5, netherite 7×7 (configurable per item); sneak for a single crop |
| Trampling | only the player holding a hoe was protected | `"never"` by default: no player or mob tramples farmland; `"with_tool"` (old behaviour) and `"vanilla"` available |
| Farming Crate area | lopsided 6×6, one layer | Centred 9×9 (radius 4, configurable), the crate's layer and one above and below |
| Crate storage | 1 item of each stack kept, rest deleted; deleted everything when full; vacuumed players' items | Every drop stored in full; a crop is only harvested when all its drops fit, so nothing is ever lost; never picks up foreign items |
| Crate automation | any face in and out | Hoppers and pipes extract from any face, nothing is pushed in; comparator output |
| Crate GUI | bare slots | Title, harvest counter, status line (working / storage full / nothing ripe) and the work area |
| Watering & growth | none | **Sprinkler** (new): fed by a water block hidden up to 4 blocks below it (or beside it), it keeps all farmland in a 9×9 area hydrated and gives the crops there extra growth ticks, with a visible spray |
| Indicators | baked-model override, off switch gone on 26.x | A built-in resource pack enabled by default, listed in the Resource Packs screen where it can be turned off or reordered; `harvest_indicators: false` removes it |
| Config | JaUML file, no comments | `config/better_farming_plus.json` with comments, no extra library; 0.5.0 values migrated automatically |
| Dependencies | JaUML required | none (Fabric API on Fabric) |
| 1.21.1 crafting / loot | broken (pre-1.21 folders) | fixed, data generated per version |
| Versions | 1.20.1, 1.21.1 | + 26.1.2, 26.2, 26.3 (Fabric and NeoForge) |
| Tests / QA | none | JUnit for the Minecraft-free rules; in-game QA script with a real server and client on all 10 builds |

## 4. The plan as built

### The headline: a farm that runs itself

Farming mods usually stop at "right-click to harvest" or jump to tech mods with power, pipes and machines. The gap
between them is a **vanilla-feeling, no-redstone farm loop**:

1. **Sprinkler**: a small copper block fed by water up to 4 blocks straight below it or beside it. A water block next
   to farmland already hydrates a 9×9 in vanilla, so the point is to hide it: two blocks under the field it hydrates
   nothing by itself, and the sprinkler on top does the watering. Every 5 seconds it waters every farmland
   block in a 9×9 square around it (from two blocks below to one above, so it works sunk into the field or on a
   post) and gives each growing crop there a 15 % chance of an extra growth tick, the same random tick vanilla uses,
   so every crop (modded too) grows by its own rules. Fields no longer need a water block every few blocks, and
   they get about three times as many growth ticks (vanilla gives a block 3/4096 random ticks per game tick, 0.073
   per 5 seconds; the sprinkler adds 0.15), so they grow about three times as fast. Spray particles show it working;
   without water it stays dry.
2. **Farming Crate**: placed in the same field, it harvests every ripe crop in its 9×9 area once a second, replants
   them with a seed from their own drops and stores everything; a hopper under it carries the harvest away.
3. **The player**: right-click with a hoe sweeps an area sized by the hoe, and nobody tramples the field.

Plant once, add a sprinkler and a crate, and the field works on its own. Both blocks fit in early game (copper and
a bucket for the sprinkler; the crate keeps its 0.5.0 recipe).

### Harvest rules (shared by the hand, the sweep and the crate)

* A block is harvestable when it is mature (its `age` property is at its maximum), it is a `CropBlock` or in
  `#better_farming_plus:harvestable` (default: nether wart, cocoa), and it is not in
  `#better_farming_plus:not_harvestable` (default: torchflower crop, which turns into a flower, not a crop).
* Drops come from the block's own loot table with the tool (Fortune applies); one item of the block's seed
  (`block.asItem()`, e.g. wheat seeds, carrot, nether wart, cocoa beans) is removed and the crop is set back to age 0
  with all its other properties (cocoa keeps its facing).
* Sweep: the clicked crop plus every harvestable crop of any kind in the square; young crops are skipped.

### Ids and saves

`better_farming_plus:farming_crate` keeps its block, item and block entity ids and its `Items` storage, so crates
in existing worlds keep their contents. The new block is `better_farming_plus:sprinkler`. The crate's menu type
changed id (`farming_crate`) because its contents changed; menus are never saved, so this is not a break.

### Code

* `common/` holds everything. `core/` is Minecraft-free (config, migration, sweep sizes, area iteration, storage
  fitting) and covered by JUnit. `farming/` applies the rules to the world. Loader modules only register content,
  events, the menu screen and the built-in resource pack.
* Data and assets are generated by `tools/datagen.py` in each Minecraft version's own format (folder names, recipe
  ingredient and result syntax, item model definitions and pack formats differ between 1.20.1 and 26.3).

### Out of scope for 1.0

* Crop info overlays (growth percentage on hover): several HUD APIs per version for something other mods cover.
* Seasons, quality tiers and new crops: content mods; Better Farming++ stays a vanilla-plus utility.
* Sweep for sugar cane, cactus, melons and pumpkins: they are not replanted crops; the crate leaves them alone too.
