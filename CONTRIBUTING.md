# Contributing

Thanks for helping improve Better Farming++.

## How to contribute

1. Fork the repository and create a feature branch from `main`.
2. Make focused changes in the relevant Minecraft version workspace (`1.20.1/`, `1.21.1/`, `26.1.2/`, `26.2/` or
   `26.3/`). A gameplay change usually belongs in all five: the `common/` code is nearly identical between them, and
   only version-specific APIs differ.
3. Build the workspace you changed (`./gradlew build` from that folder, with Java 17 for 1.20.1, 21 for 1.21.1 and 25
   for 26.x). This also runs the unit tests.
4. Open a pull request describing the change and how you tested it.

## Layout

Each workspace is a MultiLoader Gradle build:

- `common/`: everything that matters.
  - `core/` is Minecraft-free and unit-tested (`common/src/test`): the config file and the 0.5.0 migration, who may
    harvest, sweep sizes, the harvest square and whether drops fit into storage.
  - `farming/` applies the rules to the world (harvest and replant, trampling), `block/` holds the Farming Crate and
    the Sprinkler, `menu/` and `client/` the crate screen, `content/` the registrations and tags.
- `fabric/`, `neoforge/` (`forge/` on 1.20.1): entry points that register the content, the right-click and trampling
  hooks (a mixin on Fabric, events on NeoForge and Forge), the crate screen and the Harvest Indicators pack.

Assets and data are generated, not edited by hand:

```bash
python3 tools/gen_textures.py   # sprinkler textures and the crate screen, into tools/assets/
python3 tools/datagen.py        # models, item definitions, lang, loot tables, recipes, tags, the indicator pack
```

`tools/datagen.py` knows each version's format (folder names, ingredient and result syntax, loot conditions, item
model definitions, pack formats); change the tables at its top and run it, then rebuild. The indicator models live in
`tools/assets/harvest_indicators/`.

## Testing in game

`tools/qa/` drives dev servers over RCON and a real client under Xvfb (software rendering). It needs `xdotool` and
ImageMagick (`import`) and an X server on `:1` (`Xvfb :1 -screen 0 1920x1080x24 &`); 26.3 clients also need Mesa EGL
(`libegl1 libegl-mesa0 libgl1-mesa-dri libgles2`).

```bash
tools/qa/devrun.sh prepare-server 26.3 fabric     # eula, RCON on 25575 (password qa), a fresh flat world, a 0.5.0 config
tools/qa/devrun.sh start-server 26.3 fabric
tools/qa/devrun.sh start-client 26.3 fabric       # joins localhost:25565 as "Builder"
python3 tools/qa/rcon.py "op Builder"
python3 tools/qa/farming_checks.py --log /tmp/bfp-qa/fabric-server.log --client --ws 26.3 --loader fabric --shots /tmp/bfp-qa/shots
tools/qa/devrun.sh stop-client; tools/qa/devrun.sh stop-server 26.3
```

`farming_checks.py` builds every test field with commands, with random ticks off so every change comes from the mod:
the config migration, the Farming Crate (a full 9×9 of wheat plus nether wart, cocoa and a crop one layer up, the
square's edge, the block above a crop, old storage, dropped items, a hopper below, a full crate, a comparator and a
hopper above), the Sprinkler with water hidden below and without water, a mob landing on farmland, recipes and loot
tables. With `--client` the player harvests with an empty hand, sweeps with a diamond hoe, takes single crops with a
sneaking netherite hoe and a stone hoe, harvests nether wart and cocoa, leaves an unripe crop, falls on farmland, and
the client log is checked for the Harvest Indicators pack. It ends by scanning the server log for errors.

`tools/qa/run_matrix.sh` does all of that on a fresh world for every build (ten: five versions, two loaders each):

```bash
tools/qa/run_matrix.sh                       # all builds; results in /tmp/bfp-qa/results/
tools/qa/run_matrix.sh 26.3:fabric 1.20.1:forge
```

`prepare-server` writes `advertiseDedicatedServerToLan = false` to `forge/runs/server/defaultconfigs/forge-server.toml`
for the 1.20.1 Forge dev server (otherwise Forge's LAN advertiser loads a client-only class and the server stops).
Stop dev runs with `devrun.sh stop-client` / `stop-server`, which only look at Java processes.

If you change gameplay or data, run the checks on every workspace you touched, for both loaders.

## Coding expectations

- Prefer small, reviewable PRs over large mixed changes.
- Keep ids stable: block, item and block entity ids are what saved worlds refer to.
- Keep logic Minecraft-free where it can be, and test it in `common/src/test`.
- Match existing naming, package layout, and MultiLoader patterns.
- Do not commit build output, run directories, IDE metadata, or local secrets.
- When fixing a bug, say which versions and loaders you verified.

## Issues

Use GitHub Issues for bugs and feature requests. Include the Minecraft version, mod loader, mod version, other farming
or crop mods, and steps to reproduce when reporting bugs.

## Licensing

By contributing, you agree that your contributions are licensed under the Apache License, Version 2.0, unless you
state otherwise in the pull request.
