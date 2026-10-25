#!/usr/bin/env python3
"""Captures the listing screenshots from a real dev client (see marketing/README.md).

Run against a 26.3 Fabric dev server on the showcase world (level-name=showcase, level-type minecraft:normal,
level-seed SEED below, view-distance=12), with the QA client online and opped:

  python3 tools/qa/marketing_shots.py [shot...]

The fields are laid out with commands on natural terrain near a plains biome (flattened where they stand) and the
crops are set to their growth stages with commands; everything the mod does in the pictures (the spray, the crate's
harvest and sparkles, the hoe sweep, the indicators, the screen) is the mod running. Each shot is one unedited frame: a screen capture, or for the one moment too quick for that (the crate's harvest) one
frame of a lossless screen recording, picked by a fixed rule (the crate's harvest).
"""
from __future__ import annotations

import argparse
import os
import random
import re
import sys
import tempfile
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from client import Session, PLAYER  # noqa: E402

SEED = '7214950366214'
NS = 'better_farming_plus'
OUT = Path(__file__).resolve().parents[2] / 'marketing' / 'images' / 'screenshots'


class Farm:
    def __init__(self, s: Session):
        self.s = s
        self.ox = self.oz = self.gy = None

    def cmd(self, *c):
        return self.s.cmd(*c)

    def save(self, name, wait=1.5):
        time.sleep(wait)
        png = OUT / (name + '.png')
        self.s.shot(png)
        from PIL import Image
        Image.open(png).convert('RGB').save(OUT / (name + '.jpg'), quality=90)
        png.unlink()
        print('captured', name + '.jpg', flush=True)

    def surface(self, x, z, top=140, bottom=40):
        """The ground under a column: the highest dirt, grass, sand or stone block (trees are skipped)."""
        for y in range(top, bottom, -1):
            for ground in ('#minecraft:dirt', 'minecraft:sand', 'minecraft:stone'):
                if 'passed' in self.cmd(f'execute if block {x} {y} {z} {ground}'):
                    return y
        return 64

    def prepare(self):
        for rule in ('advance_time false', 'doDaylightCycle false', 'advance_weather false', 'doWeatherCycle false',
                     'spawn_mobs false', 'doMobSpawning false', 'random_tick_speed 0', 'randomTickSpeed 0'):
            self.cmd(f'gamerule {rule}')
        for rule in ('send_command_feedback false', 'sendCommandFeedback false'):
            self.cmd(f'gamerule {rule}')
        self.cmd('time set 2500', 'weather clear', f'gamemode spectator {PLAYER}', f'recipe give {PLAYER} *')
        out = self.cmd('locate biome minecraft:plains')
        m = re.search(r'\[(-?\d+), (?:~|-?\d+), (-?\d+)\]', out)
        self.ox, self.oz = (int(m.group(1)), int(m.group(2))) if m else (0, 0)
        self.cmd(f'forceload add {self.ox - 40} {self.oz - 30} {self.ox + 40} {self.oz + 30}')
        self.s.tp(self.ox + 0.5, 120, self.oz + 0.5)
        time.sleep(10)
        self.gy = self.surface(self.ox, self.oz)
        print(f'showcase at {self.ox} {self.gy} {self.oz}', flush=True)

    def flatten(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        for x0 in range(ox - 30, ox + 31, 15):
            for z0 in range(oz - 14, oz + 15, 14):
                x1, z1 = min(x0 + 14, ox + 30), min(z0 + 13, oz + 14)
                self.cmd(f'fill {x0} {gy + 1} {z0} {x1} {gy + 16} {z1} minecraft:air')
                self.cmd(f'fill {x0} {gy - 3} {z0} {x1} {gy - 1} {z1} minecraft:dirt')
                self.cmd(f'fill {x0} {gy} {z0} {x1} {gy} {z1} minecraft:grass_block')

    def field(self, cx, cz, radius, crop, ages, rnd, moisture=7):
        gy = self.gy
        self.cmd(f'fill {cx - radius - 1} {gy} {cz - radius - 1} {cx + radius + 1} {gy} {cz + radius + 1} minecraft:dirt_path')
        self.cmd(f'fill {cx - radius} {gy} {cz - radius} {cx + radius} {gy} {cz + radius} minecraft:farmland[moisture={moisture}]')
        for dx in range(-radius, radius + 1):
            for dz in range(-radius, radius + 1):
                age = rnd.choice(ages)
                self.cmd(f'setblock {cx + dx} {gy + 1} {cz + dz} {crop}[age={age}]')

    def build(self):
        rnd = random.Random(42)
        ox, oz, gy = self.ox, self.oz, self.gy
        self.flatten()
        # A: the sprinkler field, water hidden two blocks under the sprinkler.
        self.field(ox, oz, 4, 'minecraft:wheat', [3, 4, 5, 6, 7, 7], rnd)
        # On a copper rod above the crops; the water is still within 4 blocks below it.
        self.cmd(f'setblock {ox} {gy} {oz} minecraft:coarse_dirt', f'setblock {ox} {gy - 2} {oz} minecraft:water',
                 f'setblock {ox} {gy + 1} {oz} minecraft:lightning_rod[facing=up]', f'setblock {ox} {gy + 2} {oz} {NS}:sprinkler')
        # B: the crate field, a hopper under the crate and a chest under the hopper.
        bx = ox + 12
        self.field(bx, oz, 4, 'minecraft:carrots', [1, 2, 3, 4, 5, 6], rnd)
        for dz in (-3, -1, 1, 3):
            for dx in range(-4, 5):
                self.cmd(f'setblock {bx + dx} {gy + 1} {oz + dz} minecraft:potatoes[age={rnd.choice([1, 2, 3, 4, 5, 6])}]')
        self.cmd(f'setblock {bx} {gy - 1} {oz} minecraft:chest', f'setblock {bx} {gy} {oz} minecraft:hopper[facing=down]',
                 f'setblock {bx} {gy + 1} {oz} {NS}:farming_crate')
        # A second sprinkler keeps the crate field watered and growing.
        self.cmd(f'setblock {bx + 4} {gy} {oz + 4} minecraft:coarse_dirt', f'setblock {bx + 4} {gy - 2} {oz + 4} minecraft:water',
                 f'setblock {bx + 4} {gy + 1} {oz + 4} minecraft:lightning_rod[facing=up]', f'setblock {bx + 4} {gy + 2} {oz + 4} {NS}:sprinkler')
        # C: every harvestable crop, ripe, in a row.
        cz = oz + 9
        row = [('minecraft:wheat[age=7]', 'minecraft:farmland[moisture=7]'), ('minecraft:carrots[age=7]', 'minecraft:farmland[moisture=7]'),
               ('minecraft:potatoes[age=7]', 'minecraft:farmland[moisture=7]'), ('minecraft:beetroots[age=3]', 'minecraft:farmland[moisture=7]'),
               ('minecraft:nether_wart[age=3]', 'minecraft:soul_sand'), ('minecraft:sweet_berry_bush[age=3]', 'minecraft:grass_block')]
        self.cmd(f'fill {ox - 6} {gy} {cz - 1} {ox + 8} {gy} {cz + 1} minecraft:dirt_path')
        for i, (crop, soil) in enumerate(row):
            x = ox - 5 + i * 2
            self.cmd(f'setblock {x} {gy} {cz} {soil}', f'setblock {x} {gy + 1} {cz} {crop}')
        x = ox - 5 + len(row) * 2
        self.cmd(f'setblock {x} {gy + 1} {cz - 1} minecraft:jungle_log', f'setblock {x} {gy + 1} {cz} minecraft:cocoa[age=2,facing=north]')
        # D: a ripe field for the sweep.
        self.field(ox - 13, oz, 4, 'minecraft:wheat', [7], rnd)
        time.sleep(3)
        self.cmd(f'execute positioned {ox} {gy} {oz} run kill @e[type=!minecraft:player,distance=..80]')
        time.sleep(6)  # the sprinklers switch on at their next cycle

    # Shots ---------------------------------------------------------------------------------------------------------
    def cam(self, x, y, z, at, hud=False, wait=2.0):
        """Puts the camera (the player's eyes) at x, y, z looking at `at`."""
        self.s.tp(x, y - 1.62, z, facing=at)
        self.s.hud(hud)
        time.sleep(wait)

    def shot_farm(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        self.cam(ox - 5.5, gy + 7.5, oz + 13.5, (ox + 6, gy + 1, oz), wait=4)
        self.save('farm')

    def shot_sprinkler(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        self.cam(ox + 3.5, gy + 3.6, oz + 4.5, (ox + 0.5, gy + 2.3, oz + 0.5), wait=3)
        self.save('sprinkler')

    def shot_cutaway(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        # Staged: the ground in front of the sprinkler is dug out and its first row replaced with glass, a window onto
        # the soil and the water block hidden under the field.
        self.cmd(f'fill {ox - 4} {gy - 4} {oz + 2} {ox + 4} {gy + 1} {oz + 6} minecraft:air')
        self.cmd(f'fill {ox - 4} {gy - 3} {oz + 1} {ox + 4} {gy + 1} {oz + 1} minecraft:glass')
        self.cmd(f'fill {ox - 4} {gy - 4} {oz + 1} {ox + 4} {gy - 4} {oz + 6} minecraft:dirt')
        self.cmd(f'execute positioned {ox} {gy} {oz} run kill @e[type=minecraft:item,distance=..12]')
        self.cam(ox + 0.5, gy - 0.9, oz + 5.9, (ox + 0.5, gy - 0.2, oz + 0.5), wait=3)
        self.save('cutaway')

    def keep(self, frame, name):
        from PIL import Image
        Image.open(frame).convert('RGB').save(OUT / (name + '.jpg'), quality=90)
        print('captured', name + '.jpg', 'from', frame.name, flush=True)

    def shot_crate(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        bx = ox + 12
        self.cam(bx + 0.5, gy + 4.5, oz + 6.5, (bx + 0.5, gy + 1, oz + 0.5), wait=3)
        # The crate harvests within a second of the crops ripening; its sparkles last about a second, so the moment is
        # recorded and the frame with the most sparkle-green pixels is kept.
        rec = self.s.burst(BURST, 4)
        time.sleep(0.6)
        self.cmd(f'fill {bx - 4} {gy + 1} {oz - 4} {bx + 4} {gy + 1} {oz + 4} minecraft:carrots[age=7] replace minecraft:carrots')
        self.cmd(f'fill {bx - 4} {gy + 1} {oz - 4} {bx + 4} {gy + 1} {oz + 4} minecraft:potatoes[age=7] replace minecraft:potatoes')
        rec.wait()
        self.keep(max(sorted(BURST.glob('f*.png')), key=sparkle), 'crate')

    def shot_crate_gui(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        bx = ox + 12
        self.cmd(f'gamemode creative {PLAYER}', f'item replace entity {PLAYER} hotbar.0 with minecraft:air')
        self.cmd(f'setblock {bx} {gy + 3} {oz + 2} minecraft:barrier')
        self.s.tp(bx + 0.5, gy + 4, oz + 2.5, facing=(bx + 0.5, gy + 1.5, oz + 0.5))
        self.s.hud(True)
        time.sleep(2)
        self.s.hold(3, 0.12)
        time.sleep(1)
        # The field ripens while the screen is open: the crate harvests it and the screen updates live.
        self.cmd(f'fill {bx - 4} {gy + 1} {oz - 4} {bx + 4} {gy + 1} {oz + 4} minecraft:carrots[age=7] replace minecraft:carrots')
        self.cmd(f'fill {bx - 4} {gy + 1} {oz - 4} {bx + 4} {gy + 1} {oz + 4} minecraft:potatoes[age=7] replace minecraft:potatoes')
        self.save('crate_gui', wait=2.5)
        self.s.key('Escape')
        self.cmd(f'setblock {bx} {gy + 3} {oz + 2} minecraft:air', f'gamemode spectator {PLAYER}')

    def shot_sweep(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        dx = ox - 13
        self.cmd(f'gamemode creative {PLAYER}', f'item replace entity {PLAYER} hotbar.0 with minecraft:diamond_hoe')
        self.cmd(f'setblock {dx} {gy + 3} {oz + 3} minecraft:barrier')
        self.s.tp(dx + 0.5, gy + 4, oz + 3.5, facing=(dx + 0.5, gy + 1.9, oz + 0.5))
        self.s.hud(True)
        time.sleep(2.5)
        self.save('sweep_before', wait=0.5)
        self.s.hold(3, 0.12)
        self.save('sweep_after', wait=0.6)
        self.cmd(f'setblock {dx} {gy + 3} {oz + 3} minecraft:air', 'kill @e[type=minecraft:item]', f'gamemode spectator {PLAYER}')

    def shot_crops(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        self.cam(ox + 1.5, gy + 3.4, oz + 17.5, (ox + 1.5, gy + 1.6, oz + 9.5), wait=3)
        self.save('crops')

    def shot_indicators(self):
        ox, oz, gy = self.ox, self.oz, self.gy
        self.cam(ox - 3.5, gy + 3.0, oz + 6.0, (ox + 0.5, gy + 1.0, oz + 0.5), wait=3)
        self.save('indicators')


BURST = Path(os.environ.get('QA_LOGS', tempfile.gettempdir())) / 'shots-burst'


def sparkle(frame):
    """How many pixels of a frame have the harvest sparkles' bright green."""
    from PIL import Image
    im = Image.open(frame).convert('RGB').resize((480, 270))
    data = im.tobytes()
    return sum(1 for i in range(0, len(data), 3) if data[i + 1] > 200 and data[i] < 140 and data[i + 2] < 140)


SHOTS = ['farm', 'sprinkler', 'indicators', 'crops', 'crate', 'crate_gui', 'sweep', 'cutaway']


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('shots', nargs='*', default=SHOTS)
    args = ap.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    farm = Farm(Session())
    farm.prepare()
    farm.build()
    for name in args.shots:
        getattr(farm, 'shot_' + name)()
    farm.s.hud(True)


if __name__ == '__main__':
    main()
