# QA record: 1.0.0

`tools/qa/run_matrix.sh` (see [CONTRIBUTING](../CONTRIBUTING.md#testing-in-game)) on a fresh flat world per build: a
dev server and a real game client under Xvfb, driven over RCON and with real mouse and keyboard input by
`tools/qa/farming_checks.py`. Random ticks are switched off, so every change in a field comes from the mod. Every build
passed on commit `50b7579`; the only later change to mod files (`192c8c8`, removing an unused refmap reference from the
1.21.1 Fabric mixin config) was followed by a new 1.21.1 Fabric run, which passed all 47 checks. The jars in
`releases/` were built from these sources (`tools/build_release.sh`), and the unit tests (15 JUnit tests per
workspace) passed in the same builds.

| Build | Checks | Failures | Sprinkler growth stages* |
|-------|--------|----------|--------------------------|
| 1.20.1 Fabric | 47 | 0 | 12 |
| 1.20.1 Forge | 47 | 0 | 14 |
| 1.21.1 Fabric | 47 | 0 | 15 |
| 1.21.1 NeoForge | 47 | 0 | 13 |
| 26.1.2 Fabric | 47 | 0 | 10 |
| 26.1.2 NeoForge | 47 | 0 | 9 |
| 26.2 Fabric | 47 | 0 | 6 |
| 26.2 NeoForge | 47 | 0 | 18 |
| 26.3 Fabric | 47 | 0 | 10 |
| 26.3 NeoForge | 47 | 0 | 13 |

\* Growth stages gained by 80 wheat crops around one sprinkler in about 36 seconds with random ticks off (vanilla would
give none). With the default 15 % chance per crop per 5-second cycle that is about 84 extra random ticks, and wheat in
a full field advances a stage on roughly one in twelve, so about 7 stages were expected. With random ticks on, vanilla
gives the same 80 crops about 42 random ticks in that time.

The 47 checks of each build:

| Group | Checks | What |
|-------|--------|------|
| config | 3 | The 0.5.0 `config/betterfarming/global.json` was migrated: the new file is written with comments, the hoe list became `#minecraft:hoes`, and the migration is logged |
| crate | 11 | A crate in a full 9×9 of ripe wheat (plus nether wart on soul sand, cocoa on a jungle log and a crop one layer up) harvests and replants every crop, cocoa keeps its facing, the crop just outside the square stays ripe, a glass block above a crop is untouched, exactly one wheat per crop is stored, nether wart and cocoa beans are stored, three apples placed in the crate in the 0.5.0 storage format are kept, a hopper below receives items, and an emerald lying next to it is not picked up |
| crate_full | 3 | A crate whose 27 slots are full leaves the ripe crops around it standing and loses nothing; once a slot is freed it harvests |
| crate_io | 3 | A comparator next to the crate is off while it is empty and on once it holds items; a hopper above it cannot push items in |
| sprinkler | 6 | With water two blocks under the field it is active, hydrates all 80 dry farmland blocks and makes the crops grow; without water it stays inactive, hydrates nothing and grows nothing |
| trample | 2 | A husk falling five blocks onto farmland leaves it farmland, and still takes fall damage |
| client | 12 | The player right-clicks with an empty hand (crop replanted, block above kept, drops on the ground), with a diamond hoe (25 crops of a 7×7 replanted, the outer ring left, 25 durability used), with a sneaking netherite hoe and with a stone hoe (one crop each), on nether wart and on cocoa (replanted), and on an unripe crop (left alone); falls onto farmland without trampling it; and the client's resource reload includes the Harvest Indicators pack |
| data | 5 | Both recipes load; breaking the crate drops the crate (loot table) and its contents; breaking the sprinkler drops it |
| log | 2 | The server log has no errors about the mod and no errors at all |

Screenshots from the runs (the crate screen, the sweep, the indicators) were looked at for every version during
development; the listing screenshots are from 26.3 Fabric (see `marketing/README.md`).

## Production jars

The dev runs use deobfuscated Minecraft, so the three release jars that are remapped for obfuscated Minecraft were
also run on real servers: the Fabric server launcher (loader 0.16.9) with Fabric API for 1.20.1 and 1.21.1, and the
Forge 1.20.1 installer's server (Forge 47.4.0), each with the jar from `releases/`, a fresh flat world and a 0.5.0
config. `farming_checks.py` without the client (config migration, crate, full crate, crate I/O, sprinkler, trampling,
data, server log):

| Jar | Checks | Failures |
|-----|--------|----------|
| `better_farming_plus-fabric-1.20.1-1.0.0.jar` | 33 | 0 |
| `better_farming_plus-fabric-1.21.1-1.0.0.jar` | 33 | 0 |
| `better_farming_plus-forge-1.20.1-1.0.0.jar` | 33 | 0 |

The 26.x builds run unobfuscated Minecraft and NeoForge runs Mojang names at runtime, so their dev runs already use
the production names. `.github/scripts/detect-jar-meta.js` reads the loader and Minecraft version of all ten jars
correctly.

## Found and fixed by this QA

* **1.20.1 Forge: hoppers could push items into the crate.** Forge hands hoppers an unsided item handler for container
  block entities, which ignored the crate's "nothing goes in" rule. The Forge build now uses a crate block entity that
  hands out the sided view.
* **26.1.2 and 26.2 Fabric: the server did not start.** The trampling mixin targeted `FarmlandBlock#turnToBaseBlock`
  (26.3); on 26.1.2 and 26.2 the method is the static `turnToDirt`. Each version now targets its own method.
* **NeoForge and Forge listed the Harvest Indicators pack but left it off.** The pack is now switched on once, on the
  first launch, and the options are saved; turning it off later sticks.
* **Fabric could lose the pack on the next launch.** Fabric enables a default pack in memory and records that it
  offered it, but does not save the options; a client closed before its options were saved came back without the
  pack. Fabric now does the same once-only selection and save.
* **The sprinkler's spray was almost invisible** (found while taking the listing screenshots): it relied on vanilla's
  random display ticks, which pick a given block about once every few seconds. It now sprays from a client-side ticker.
* **The crate screen said "Harvesting" for one second at a time.** It now says so for ten seconds after the last
  harvest.
* The QA run also caught its own setup mistakes (a crop popped by placing a hopper under it, a flat world without
  generator settings on 26.3, a sneak key released too early); those were fixed in the script, not the mod.
