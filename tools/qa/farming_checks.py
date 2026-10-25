#!/usr/bin/env python3
"""In-game checks for Better Farming++ against a running dev server (see devrun.sh), optionally with the QA client.
Every test field is built with commands on a fresh flat world; random ticks are turned off so that every change
comes from the mod.

Server checks:
  * config: the 0.5.0 file was migrated into config/better_farming_plus.json;
  * crate: a Farming Crate in a full 9x9 field harvests and replants every ripe crop (wheat, nether wart, cocoa, one
    layer above), leaves the crop outside its square and the block above a crop alone, keeps the items it had
    (0.5.0 storage format), never picks up dropped items, and feeds a hopper below it;
  * crate_full: a full crate leaves ripe crops standing and nothing is lost; with room again it harvests;
  * crate_io: a comparator reads the crate; a hopper above cannot push items into it;
  * sprinkler: with water hidden two blocks below, it hydrates all 80 farmland blocks and grows the crops;
    without water it stays inactive and changes nothing;
  * trample: a mob falling on farmland leaves it intact and still takes fall damage;
  * data: recipes and loot tables (breaking the blocks drops them and the crate's contents).
Client checks (--client): the player harvests with an empty hand (block above kept, drops appear), sweeps 5x5 with
a diamond hoe (25 durability), takes one crop with a sneaking netherite hoe and with a stone hoe, harvests nether
wart and cocoa, leaves an unripe crop alone, falls on farmland without trampling it, and the client loaded the
Harvest Indicators pack. Screenshots are taken along the way.
The server log is then scanned for errors.

  python3 tools/qa/farming_checks.py --log /tmp/bfp-qa/fabric-server.log [--client --ws 26.3 --loader fabric --shots DIR]
"""
from __future__ import annotations

import argparse
import re
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from rcon import wait_for  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
NS = 'better_farming_plus'
PLAYER = 'Builder'
G = -61          # farmland layer of the flat world; crops stand on G + 1
C = G + 1
FAILURES: list[str] = []
COUNT = [0]


def check(name, ok, detail=''):
    COUNT[0] += 1
    print(('PASS ' if ok else 'FAIL ') + name + (f'  [{str(detail)[:400]}]' if detail and not ok else ''), flush=True)
    if not ok:
        FAILURES.append(name)
    return ok


class Server:
    def __init__(self, port, legacy):
        self.rcon = wait_for(port)
        self.legacy = legacy  # 1.20.1 NBT and data formats

    def cmd(self, command):
        return self.rcon.command(command)

    def many(self, commands):
        for c in commands:
            self.cmd(c)

    def gamerule(self, camel, snake, value):
        out = self.cmd(f'gamerule {camel} {value}')
        if 'Unknown' in out or 'Incorrect' in out or 'expected' in out.lower():
            out = self.cmd(f'gamerule {snake} {value}')
        return out

    def test(self, x, y, z, block):
        return 'passed' in self.cmd(f'execute if block {x} {y} {z} {block}')

    def items(self, x, y, z):
        """{item id: count} of a container's Items."""
        return count_items(self.cmd(f'data get block {x} {y} {z} Items'))

    def entities(self, selector):
        """How many entities match (through a scoreboard: `execute if entity` only reports pass or fail)."""
        self.cmd('scoreboard objectives add bfp dummy')
        self.cmd(f'execute store result score #n bfp if entity {selector}')
        m = re.search(r'has (\d+)', self.cmd('scoreboard players get #n bfp'))
        return int(m.group(1)) if m else 0

    def age(self, x, y, z, block, max_age):
        for age in range(max_age + 1):
            if self.test(x, y, z, f'{block}[age={age}]'):
                return age
        return None


def count_items(text):
    totals: dict[str, int] = {}
    for obj in re.findall(r'\{[^{}]*id: "[^"]+"[^{}]*\}', text):
        item = re.search(r'id: "([^"]+)"', obj).group(1)
        m = re.search(r'\b[cC]ount: (\d+)', obj)
        totals[item] = totals.get(item, 0) + (int(m.group(1)) if m else 1)
    return totals


def field(s, x0, z0, radius, crop='minecraft:wheat[age=7]', moisture=7):
    s.cmd(f'fill {x0 - radius} {G - 2} {z0 - radius} {x0 + radius} {G - 1} {z0 + radius} minecraft:dirt')
    s.cmd(f'fill {x0 - radius} {G} {z0 - radius} {x0 + radius} {G} {z0 + radius} minecraft:farmland[moisture={moisture}]')
    s.cmd(f'fill {x0 - radius} {C} {z0 - radius} {x0 + radius} {C + 3} {z0 + radius} minecraft:air')
    if crop:
        s.cmd(f'fill {x0 - radius} {C} {z0 - radius} {x0 + radius} {C} {z0 + radius} {crop}')


def square(x0, z0, radius):
    return [(x0 + dx, z0 + dz) for dx in range(-radius, radius + 1) for dz in range(-radius, radius + 1)]


# Server checks -------------------------------------------------------------------------------------------------------

def check_config():
    cfg = ROOT / ARGS.ws / ARGS.loader / 'runs' / 'server' / 'config' / 'better_farming_plus.json' if ARGS.ws else None
    if cfg:
        text = cfg.read_text() if cfg.exists() else ''
        check('config: written with comments', text.startswith('// Better Farming++.'), str(cfg))
        check('config: 0.5.0 hoe list migrated to #minecraft:hoes', '"#minecraft:hoes"' in text)
    log = Path(ARGS.log).read_text(errors='replace')
    check('config: migration logged', 'Migrated' in log and 'betterfarming' in log)


def check_crate(s):
    x0, z0 = 40, 0
    field(s, x0, z0, 4)
    field(s, x0 + 5, z0, 0)                                  # just outside the 9x9
    s.cmd(f'setblock {x0} {C} {z0} minecraft:air')
    s.cmd(f'setblock {x0} {G} {z0} minecraft:hopper[facing=down]')
    s.cmd(f'setblock {x0} {G - 1} {z0} minecraft:stone')
    s.cmd(f'setblock {x0 + 2} {C + 1} {z0 + 2} minecraft:glass')       # above a crop
    s.cmd(f'setblock {x0 + 3} {C} {z0 + 3} minecraft:farmland[moisture=7]')
    s.cmd(f'setblock {x0 + 3} {C + 1} {z0 + 3} minecraft:wheat[age=7]')  # one layer up
    s.cmd(f'setblock {x0 - 4} {G} {z0 - 4} minecraft:soul_sand')
    s.cmd(f'setblock {x0 - 4} {C} {z0 - 4} minecraft:nether_wart[age=3]')
    s.cmd(f'setblock {x0 + 4} {C} {z0 - 4} minecraft:jungle_log')
    s.cmd(f'setblock {x0 + 4} {C} {z0 - 3} minecraft:cocoa[age=2,facing=north]')
    apple = '{Slot:26b,id:"minecraft:apple",Count:3b}' if s.legacy else '{Slot:26b,id:"minecraft:apple",count:3}'
    s.cmd(f'setblock {x0} {C} {z0} {NS}:farming_crate{{Items:[{apple}]}}')
    s.cmd(f'summon minecraft:item {x0 - 2}.5 {C + 1} {z0 + 1}.5 {{Item:{{id:"minecraft:emerald",{"Count:1b" if s.legacy else "count:1"}}},PickupDelay:32767,Tags:["bfp_keep"]}}')
    time.sleep(6)
    wheat = [p for p in square(x0, z0, 4) if p not in ((x0, z0), (x0 + 3, z0 + 3), (x0 - 4, z0 - 4), (x0 + 4, z0 - 4), (x0 + 4, z0 - 3))]
    replanted = sum(s.test(x, C, z, 'minecraft:wheat[age=0]') for x, z in wheat)
    check('crate: every ripe wheat in the 9x9 replanted', replanted == len(wheat), f'{replanted}/{len(wheat)}')
    check('crate: crop one layer up harvested', s.test(x0 + 3, C + 1, z0 + 3, 'minecraft:wheat[age=0]'))
    check('crate: nether wart harvested and replanted', s.test(x0 - 4, C, z0 - 4, 'minecraft:nether_wart[age=0]'))
    check('crate: cocoa harvested and replanted (facing kept)', s.test(x0 + 4, C, z0 - 3, 'minecraft:cocoa[age=0,facing=north]'))
    check('crate: crop outside the square left alone', s.test(x0 + 5, C, z0, 'minecraft:wheat[age=7]'))
    check('crate: block above a crop untouched', s.test(x0 + 2, C + 1, z0 + 2, 'minecraft:glass'))
    inv = s.items(x0, C, z0)
    hopper = s.items(x0, G, z0)
    total = {k: inv.get(k, 0) + hopper.get(k, 0) for k in set(inv) | set(hopper)}
    check('crate: stored one wheat per crop', total.get('minecraft:wheat', 0) == len(wheat) + 1, total)
    check('crate: stored nether wart and cocoa beans', total.get('minecraft:nether_wart', 0) >= 1 and total.get('minecraft:cocoa_beans', 0) >= 1, total)
    check('crate: kept the items it already had (0.5.0 storage)', total.get('minecraft:apple', 0) == 3, total)
    check('crate: hopper below takes from it', sum(hopper.values()) > 0, hopper)
    check('crate: never picks up dropped items', s.entities('@e[type=minecraft:item,tag=bfp_keep]') == 1)
    s.cmd('kill @e[type=minecraft:item,tag=bfp_keep]')
    return x0, z0


def check_crate_full(s):
    x0, z0 = 40, 30
    field(s, x0, z0, 1)
    s.cmd(f'setblock {x0} {C} {z0} {NS}:farming_crate')
    for slot in range(27):
        s.cmd(f'item replace block {x0} {C} {z0} container.{slot} with minecraft:cobblestone 64')
    time.sleep(4)
    ripe = sum(s.test(x, C, z, 'minecraft:wheat[age=7]') for x, z in square(x0, z0, 1) if (x, z) != (x0, z0))
    check('crate_full: ripe crops left standing while full', ripe == 8, f'{ripe}/8')
    check('crate_full: nothing lost', s.items(x0, C, z0) == {'minecraft:cobblestone': 27 * 64})
    s.cmd(f'item replace block {x0} {C} {z0} container.0 with minecraft:air')
    time.sleep(4)
    ripe = sum(s.test(x, C, z, 'minecraft:wheat[age=0]') for x, z in square(x0, z0, 1) if (x, z) != (x0, z0))
    check('crate_full: harvests again once there is room', ripe >= 1, f'{ripe} replanted')
    return x0, z0


def check_crate_io(s):
    x0, z0 = 40, 60
    s.cmd(f'fill {x0 - 1} {G} {z0 - 1} {x0 + 2} {G} {z0 + 1} minecraft:stone')
    s.cmd(f'fill {x0 - 1} {C} {z0 - 1} {x0 + 2} {C + 2} {z0 + 1} minecraft:air')
    s.cmd(f'setblock {x0} {C} {z0} {NS}:farming_crate')
    s.cmd(f'setblock {x0 + 1} {C} {z0} minecraft:comparator[facing=west]')
    check('crate_io: comparator off while empty', s.test(x0 + 1, C, z0, 'minecraft:comparator[powered=false]'))
    s.cmd(f'item replace block {x0} {C} {z0} container.0 with minecraft:stone 64')
    s.cmd(f'setblock {x0} {C + 1} {z0} minecraft:hopper[facing=down]')
    s.cmd(f'item replace block {x0} {C + 1} {z0} container.0 with minecraft:apple 5')
    time.sleep(3)
    check('crate_io: comparator reads its contents', s.test(x0 + 1, C, z0, 'minecraft:comparator[powered=true]'))
    check('crate_io: a hopper cannot push items into it', s.items(x0, C + 1, z0).get('minecraft:apple', 0) == 5
          and 'minecraft:apple' not in s.items(x0, C, z0), (s.items(x0, C + 1, z0), s.items(x0, C, z0)))


def check_sprinkler(s):
    results = {}
    for label, (x0, z0), water in (('wet', (80, 0), True), ('dry', (120, 0), False)):
        field(s, x0, z0, 4, crop='minecraft:wheat[age=0]', moisture=0)
        s.cmd(f'setblock {x0} {G} {z0} minecraft:dirt')
        s.cmd(f'setblock {x0} {G - 1} {z0} {"minecraft:water" if water else "minecraft:dirt"}')
        s.cmd(f'setblock {x0} {C} {z0} {NS}:sprinkler')
        results[label] = (x0, z0)
    time.sleep(36)
    for label, (x0, z0) in results.items():
        cells = [p for p in square(x0, z0, 4) if p != (x0, z0)]
        wet = sum(s.test(x, G, z, 'minecraft:farmland[moisture=7]') for x, z in cells)
        ages = [s.age(x, C, z, 'minecraft:wheat', 7) or 0 for x, z in cells]
        if label == 'wet':
            check('sprinkler: active with water hidden below', s.test(x0, C, z0, f'{NS}:sprinkler[active=true]'))
            check('sprinkler: hydrated all 80 farmland blocks', wet == 80, f'{wet}/80')
            check('sprinkler: crops grew (random ticks are off)', sum(ages) >= 3, f'age sum {sum(ages)}')
            print(f'INFO sprinkler: {sum(ages)} growth stages over 80 crops in ~36 s')
        else:
            check('sprinkler: inactive without water', s.test(x0, C, z0, f'{NS}:sprinkler[active=false]'))
            check('sprinkler: no water, no hydration', wet == 0, f'{wet}/80')
            check('sprinkler: no water, no growth', sum(ages) == 0, f'age sum {sum(ages)}')
    return results['dry']


def check_trample(s):
    x0, z0 = 0, 40
    field(s, x0, z0, 1, crop=None)
    s.cmd(f'summon minecraft:husk {x0}.5 {C + 5} {z0}.5 {{Tags:["bfp_fall"],PersistenceRequired:1b}}')
    time.sleep(4)
    check('trample: a mob landing on farmland leaves it', s.test(x0, G, z0, 'minecraft:farmland'))
    out = s.cmd('data get entity @e[type=minecraft:husk,tag=bfp_fall,limit=1] Health')
    m = re.search(r'([\d.]+)f?\s*$', out.strip())
    check('trample: the fall still hurts', m is not None and float(m.group(1)) < 20, out)
    s.cmd('kill @e[type=minecraft:husk,tag=bfp_fall]')


def check_data(s, crate, sprinkler):
    for rid in ('crates_recipe', 'sprinkler'):
        out = s.cmd(f'recipe give {PLAYER} {NS}:{rid}') if ARGS.client else s.cmd(f'recipe give @a {NS}:{rid}')
        check(f'data: recipe {rid} exists', 'Unknown' not in out and 'nvalid' not in out, out)
    x, z = crate
    stored = s.items(x, C, z).get('minecraft:cobblestone', 0)
    s.cmd('kill @e[type=minecraft:item]')
    s.cmd(f'setblock {x} {C} {z} minecraft:air destroy')
    time.sleep(1)
    near = f'x={x},y={C},z={z},distance=..4'
    check('data: breaking the crate drops it (loot table)', s.entities(f'@e[type=minecraft:item,{near},nbt={{Item:{{id:"{NS}:farming_crate"}}}}]') == 1)
    dropped = s.entities(f'@e[type=minecraft:item,{near},nbt={{Item:{{id:"minecraft:cobblestone"}}}}]')
    check('data: breaking the crate drops its contents', stored >= 25 * 64 and dropped >= 25, f'stored {stored}, {dropped} cobblestone item entities')
    x, z = sprinkler
    s.cmd(f'setblock {x} {C} {z} minecraft:air destroy')
    time.sleep(1)
    check('data: breaking the sprinkler drops it', s.entities(f'@e[type=minecraft:item,x={x},y={C},z={z},distance=..3,nbt={{Item:{{id:"{NS}:sprinkler"}}}}]') == 1)
    s.cmd('kill @e[type=minecraft:item]')


# Client checks -------------------------------------------------------------------------------------------------------

def use_on(cs, x, z, sneak=False, settle=1.5):
    """Stand on a glass block two south and two up, look at the crop at (x, C, z) and right-click it."""
    cs.cmd(f'setblock {x} {C + 2} {z + 2} minecraft:glass')
    cs.tp(x + 0.5, C + 3, z + 2.5, facing=(x + 0.5, C + 0.9, z + 0.5))
    time.sleep(1.0)
    cs.hold(3, 0.12, sneak=sneak)
    time.sleep(settle)


def hand(cs, item):
    cs.cmd(f'item replace entity {PLAYER} hotbar.0 with {item}')
    time.sleep(0.5)


def damage(cs):
    out = cs.cmd(f'data get entity {PLAYER} Inventory[0]')
    m = re.search(r'(?:minecraft:damage"?|Damage): (\d+)', out)
    return int(m.group(1)) if m else 0


def check_client(s, shots):
    from client import Session
    cs = Session()
    cs.cmd(f'gamemode survival {PLAYER}', f'effect give {PLAYER} minecraft:resistance 99999 4 true',
           f'effect give {PLAYER} minecraft:saturation 99999 1 true')
    z0 = 80
    # Empty hand, one crop, glass above it.
    field(s, -10, z0, 0)
    s.cmd(f'setblock -10 {C + 1} {z0} minecraft:glass')
    hand(cs, 'minecraft:air')
    cs.tp(-9.5, C + 3, z0 + 2.5, facing=(-9.5, C + 0.9, z0 + 0.5))
    time.sleep(8)  # first teleport: let the client load the terrain
    s.cmd(f'setblock -10 {C + 1} {z0} minecraft:air')
    use_on(cs, -10, z0)
    check('client: empty hand harvests and replants', s.test(-10, C, z0, 'minecraft:wheat[age=0]'))
    check('client: drops appear where the crop stood', s.entities(f'@e[type=minecraft:item,x=-10,y={C},z={z0},distance=..2]') >= 1)
    s.cmd('kill @e[type=minecraft:item]')

    # Diamond hoe: 5x5 of a 7x7 field.
    field(s, 0, z0, 3)
    hand(cs, 'minecraft:diamond_hoe')
    use_on(cs, 0, z0, settle=2.0)
    inner = sum(s.test(x, C, z, 'minecraft:wheat[age=0]') for x, z in square(0, z0, 2))
    ring = sum(s.test(x, C, z, 'minecraft:wheat[age=7]') for x, z in square(0, z0, 3) if max(abs(x), abs(z - z0)) == 3)
    check('client: diamond hoe sweeps 5x5', inner == 25, f'{inner}/25')
    check('client: crops beyond the sweep left standing', ring == 24, f'{ring}/24')
    check('client: hoe loses 1 durability per crop', damage(cs) == 25, damage(cs))
    if shots:
        cs.tp(0.5, C + 6, z0 + 8.5, facing=(0.5, C, z0 + 0.5))
        time.sleep(1.5)
        cs.hud(False)
        cs.shot(Path(shots) / 'sweep.png')
        cs.hud(True)
    s.cmd('kill @e[type=minecraft:item]')

    # Sneaking with a netherite hoe takes one crop; a stone hoe takes one crop.
    for label, x0, item, sneak in (('sneaking netherite hoe', 20, 'minecraft:netherite_hoe', True), ('stone hoe', 30, 'minecraft:stone_hoe', False)):
        field(s, x0, z0, 1)
        hand(cs, item)
        use_on(cs, x0, z0, sneak=sneak)
        centre = s.test(x0, C, z0, 'minecraft:wheat[age=0]')
        others = sum(s.test(x, C, z, 'minecraft:wheat[age=7]') for x, z in square(x0, z0, 1) if (x, z) != (x0, z0))
        check(f'client: {label} takes only the clicked crop', centre and others == 8, f'centre={centre} others={others}/8')

    # Nether wart and cocoa by hand; an unripe crop is left alone.
    hand(cs, 'minecraft:air')
    s.cmd(f'fill 38 {G - 1} {z0 - 2} 62 {C + 3} {z0 + 2} minecraft:air')
    s.cmd(f'fill 38 {G - 1} {z0 - 2} 62 {G - 1} {z0 + 4} minecraft:dirt')
    s.cmd(f'setblock 40 {G} {z0} minecraft:soul_sand')
    s.cmd(f'setblock 40 {C} {z0} minecraft:nether_wart[age=3]')
    use_on(cs, 40, z0)
    check('client: nether wart harvested and replanted', s.test(40, C, z0, 'minecraft:nether_wart[age=0]'))
    s.cmd(f'setblock 50 {C} {z0 - 1} minecraft:jungle_log')
    s.cmd(f'setblock 50 {C} {z0} minecraft:cocoa[age=2,facing=north]')
    use_on(cs, 50, z0)
    check('client: cocoa harvested and replanted', s.test(50, C, z0, 'minecraft:cocoa[age=0,facing=north]'))
    field(s, 60, z0, 0, crop='minecraft:wheat[age=3]')
    use_on(cs, 60, z0)
    check('client: unripe crop left alone', s.test(60, C, z0, 'minecraft:wheat[age=3]'))
    s.cmd('kill @e[type=minecraft:item]')

    # The player falls on farmland: no trampling.
    field(s, 0, 120, 1, crop=None)
    hand(cs, 'minecraft:air')
    cs.cmd(f'tp {PLAYER} 0.5 {C + 6} 120.5')
    time.sleep(3)
    check('client: player landing on farmland does not trample it', s.test(0, G, 120, 'minecraft:farmland'))

    # The Harvest Indicators pack is part of the client's resources.
    client_log = ROOT / ARGS.ws / ARGS.loader / 'runs' / 'client' / 'logs' / 'latest.log'
    text = client_log.read_text(errors='replace') if client_log.exists() else ''
    reload_lines = [l for l in text.splitlines() if 'Reloading ResourceManager' in l]
    check('client: Harvest Indicators pack enabled by default', any('harvest_indicators' in l for l in reload_lines),
          reload_lines[-1][-300:] if reload_lines else client_log)
    if shots:
        field(s, 0, 160, 3)
        s.cmd(f'fill -3 {C} 160 3 {C} 160 minecraft:wheat[age=4]')
        cs.cmd(f'gamemode spectator {PLAYER}')
        cs.tp(0.5, C + 2.5, 165.5, facing=(0.5, C + 0.5, 160.5))
        time.sleep(2)
        cs.hud(False)
        cs.shot(Path(shots) / 'indicators.png')
        cs.hud(True)
        cs.cmd(f'gamemode survival {PLAYER}')
    return cs


def check_crate_gui(cs, shots):
    s = cs
    field(s, 40, 200, 2)
    s.cmd(f'setblock 40 {C} 200 {NS}:farming_crate')
    time.sleep(3)
    s.cmd(f'setblock 40 {C + 2} 202 minecraft:glass')
    s.tp(40.5, C + 3, 202.5, facing=(40.5, C + 0.5, 200.5))
    time.sleep(1.5)
    s.hold(3, 0.12)
    time.sleep(1.5)
    if shots:
        s.shot(Path(shots) / 'crate_gui.png')
    s.key('Escape')
    time.sleep(0.5)


def check_log(path):
    text = Path(path).read_text(errors='replace')
    lines = text.splitlines()
    ours = [l for l in lines if ('ERROR' in l or 'Exception' in l) and ('better_farming_plus' in l or 'betterfarmingplus' in l)]
    check('log: no errors about the mod', not ours, ours[:3])
    errors = [l for l in lines if '/ERROR]' in l or ' ERROR ' in l or 'Exception' in l]
    check('log: no errors at all', not errors, errors[:3])


def main():
    global ARGS
    ap = argparse.ArgumentParser()
    ap.add_argument('--port', type=int, default=25575)
    ap.add_argument('--log', required=True)
    ap.add_argument('--client', action='store_true')
    ap.add_argument('--ws', default='')
    ap.add_argument('--loader', default='')
    ap.add_argument('--minecraft', default='', help='Minecraft version for NBT syntax when there is no --ws (production servers)')
    ap.add_argument('--shots')
    ARGS = ap.parse_args()
    s = Server(ARGS.port, legacy=(ARGS.minecraft or ARGS.ws) == '1.20.1')
    s.many([
        'forceload add -16 -16 140 220', 'time set noon', 'weather clear',
    ])
    s.gamerule('randomTickSpeed', 'random_tick_speed', 0)
    s.gamerule('doDaylightCycle', 'advance_time', 'false')
    s.gamerule('doWeatherCycle', 'advance_weather', 'false')
    s.gamerule('doMobSpawning', 'spawn_mobs', 'false')
    time.sleep(5)
    check_config()
    crate = check_crate(s)
    full = check_crate_full(s)
    check_crate_io(s)
    dry = check_sprinkler(s)
    check_trample(s)
    if ARGS.client:
        cs = check_client(s, ARGS.shots)
        check_crate_gui(cs, ARGS.shots)
    check_data(s, full, dry)
    check_log(ARGS.log)
    print(f'{COUNT[0]} checks, {len(FAILURES)} failures' + (': ' + ', '.join(FAILURES) if FAILURES else ''))
    sys.exit(1 if FAILURES else 0)


if __name__ == '__main__':
    main()
