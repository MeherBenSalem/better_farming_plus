# Marketing

Listing copy and artwork for CurseForge and Modrinth.

| Path | What it is |
|------|------------|
| `curseforge/description.md` | The project description (Markdown; also works on Modrinth). Images load from this repo's `main` branch. |
| `curseforge/summary.txt` | The one-line summary (under 250 characters). |
| `images/banner.png` | 1800×600 header banner. |
| `images/icon.png` | 512×512 project icon (also the mod's in-jar `logo.png`). |
| `images/card-*.png` | 1600×900 feature cards used in the description and as gallery images. |
| `images/header-*.png`, `images/divider.png` | Transparent section headers and a divider for the description. |
| `images/screenshots/*.jpg` | Real in-game captures (Minecraft 26.3, Fabric) used by the cards and the gallery. |
| `source/render.cjs` | Renders every PNG above from the screenshots and the bundled fonts. |
| `source/config-excerpt.json` | The config file as the mod writes it on first start, shown on the config card. |
| `source/fonts/` | Fraunces and Nunito, both under the SIL Open Font License (licence files included). |

The look: a sunlit field. Rich soil brown behind everything, furrow stripes, a warm sun glow, wheat-gold titles in
Fraunces, cream-bordered photo frames with rounded corners, seed-shaped bullets, and one colour per feature (leaf
green for the farm loop and the config, water blue for the Sprinkler, crate-wood tan for the Farming Crate, berry red
for the indicators). The icon is the mod's "++" on a leaf-green disc over a real capture of ripe crops.

## Regenerating the images

Needs Node.js and Playwright with Chromium:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs             # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs card-crate  # one asset
```

The release name and the supported Minecraft versions and loaders are at the top of `render.cjs`. Supporting a new
Minecraft version means adding one line there, plus one row to the download table in `description.md`. After
changing the icon, copy `images/icon.png` to every workspace's `common/src/main/resources/logo.png`.

## Retaking the screenshots

`tools/qa/marketing_shots.py` captures every screenshot from a dev client (see `tools/qa/` and `CONTRIBUTING.md`): a
1920×1080 window under Xvfb, on a 26.3 Fabric dev server whose `server.properties` uses `level-name=showcase`,
`level-type=minecraft:normal`, the seed given at the top of the script and `view-distance=12`:

```bash
tools/qa/devrun.sh prepare-server 26.3 fabric showcase minecraft:normal 7214950366214
sed -i 's/view-distance=8/view-distance=12/' 26.3/fabric/runs/server/server.properties
tools/qa/devrun.sh start-server 26.3 fabric && tools/qa/devrun.sh start-client 26.3 fabric
python3 tools/qa/rcon.py "op Builder"
python3 tools/qa/marketing_shots.py              # or a few: farm crate sweep
```

The script finds a plains biome near spawn, flattens the ground where the fields go and lays them out with commands
(farmland, crops at chosen growth stages, a water block under each sprinkler, a hopper and chest under the crate).
Everything the mod does in the pictures is the mod running: the sprinkler spray, the crate harvesting with its
sparkles, the hoe sweep and its drops, the indicators and the crate screen. Every screenshot is a single unedited
frame: a screen capture, or for `crate.jpg` (the harvest sparkles last about a second, a capture takes longer) one
frame of a lossless ffmpeg screen recording, the one with the most sparkle-green pixels. For `cutaway.jpg` only, the
script digs out the ground in front of the sprinkler and puts a row of glass in front of the water block under it
(the card says so). The sprinklers stand on a lightning rod above the crops, still within 4 blocks of their water.

| Screenshot | Scene |
|------------|-------|
| `farm.jpg` | The showcase farm: a sprinkler field, a crate field (hopper and chest under the crate) with a second sprinkler, a sweep field and a row of crops |
| `sprinkler.jpg` | A sprinkler spraying over its wheat |
| `cutaway.jpg` | The same sprinkler with the ground in front dug out behind glass, showing the water block below it |
| `crate.jpg` | The crate field the moment the crate harvests it (green sparkles on every harvested crop) |
| `crate_gui.jpg` | The crate's screen while it harvests the field again |
| `sweep_before.jpg`, `sweep_after.jpg` | A ripe field before and right after one right-click with a diamond hoe |
| `crops.jpg` | Ripe wheat, carrots, potatoes, beetroots, nether wart, sweet berries and cocoa with their indicators |
| `indicators.jpg` | The sprinkler field from low down: markers above the ripe wheat only |
