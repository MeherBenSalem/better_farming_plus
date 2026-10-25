#!/usr/bin/env python3
"""Writes the mod's assets and data into every workspace, each in the format of its Minecraft version. Edit the
tables here, not the generated JSON.

  python3 tools/gen_textures.py && python3 tools/datagen.py

Format differences handled here (checked against each version's vanilla data):
  1.20.1   plural folders (loot_tables, recipes, advancements, tags/blocks), {"item": id} / {"tag": id} ingredients,
           {"item": id} results, inventory_changed "items": [ids]
  1.21.1   singular folders, {"id": id} results, "items": "#tag" predicates
  26.x     ingredients as plain strings ("#minecraft:planks"), item model definitions under assets/<ns>/items,
           min_format/max_format pack metadata
  26.3     loot "condition" (with "type") instead of "conditions", recipe_unlocked "recipes"
"""
from __future__ import annotations

import json
import shutil
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
TOOLS = Path(__file__).resolve().parent
NS = 'better_farming_plus'
VERSIONS = ['1.20.1', '1.21.1', '26.1.2', '26.2', '26.3']

# Main pack metadata (covers assets and data) and the built-in "Harvest Indicators" resource pack.
PACK = {
    '1.20.1': {'pack_format': 15},
    '1.21.1': {'pack_format': 34},
    '26.1.2': {'min_format': 84, 'max_format': 101},
    '26.2': {'min_format': 88, 'max_format': 121},
    '26.3': {'min_format': 88, 'max_format': 121},
}
INDICATOR_PACK_FORMAT = {
    '1.20.1': {'pack_format': 15},
    '1.21.1': {'pack_format': 34},
    '26.1.2': {'min_format': 84, 'max_format': 84},
    '26.2': {'min_format': 84, 'max_format': 97},
    '26.3': {'min_format': 84, 'max_format': 97},
}

LANG = {
    'block.better_farming_plus.farming_crate': 'Farming Crate',
    'block.better_farming_plus.sprinkler': 'Sprinkler',
    'item.better_farming_plus.farming_crate': 'Farming Crate',
    'item.better_farming_plus.sprinkler': 'Sprinkler',
    'container.better_farming_plus.farming_crate': 'Farming Crate',
    'gui.better_farming_plus.crate.working': 'Harvesting',
    'gui.better_farming_plus.crate.full': 'Storage full!',
    'gui.better_farming_plus.crate.idle': 'Nothing ripe',
    'gui.better_farming_plus.crate.harvested': 'Harvested: %s',
    'gui.better_farming_plus.crate.area': 'Area %sx%s',
    'pack.better_farming_plus.harvest_indicators': 'Harvest Indicators',
}

RECIPES = {
    # id: (pattern, key, result, unlocked by)
    'crates_recipe': (['aba', 'cdc', 'aea'],
                      {'a': '#minecraft:planks', 'b': 'minecraft:redstone_block', 'c': 'minecraft:comparator',
                       'd': 'minecraft:diamond_hoe', 'e': 'minecraft:chest'},
                      f'{NS}:farming_crate', '#minecraft:hoes'),
    'sprinkler': ([' c ', 'cbc', ' c '], {'c': 'minecraft:copper_ingot', 'b': 'minecraft:bucket'},
                  f'{NS}:sprinkler', 'minecraft:copper_ingot'),
}

BLOCK_TAGS = {
    f'{NS}:harvestable': ['minecraft:nether_wart', 'minecraft:cocoa'],
    f'{NS}:not_harvestable': ['minecraft:torchflower_crop'],
    f'{NS}:sprinkler_boosted': ['#minecraft:crops', 'minecraft:sweet_berry_bush', 'minecraft:cocoa', 'minecraft:nether_wart',
                                'minecraft:sugar_cane'],
    'minecraft:mineable/axe': [f'{NS}:farming_crate'],
    'minecraft:mineable/pickaxe': [f'{NS}:sprinkler'],
}

SPRINKLER_MODEL = {
    'parent': 'minecraft:block/block',
    'textures': {'side': f'{NS}:block/sprinkler', 'top': f'{NS}:block/sprinkler_top', 'particle': f'{NS}:block/sprinkler'},
    'elements': [
        {'from': [6, 0, 6], 'to': [10, 9, 10], 'faces': {
            f: {'uv': [6, 7, 10, 16], 'texture': '#side'} for f in ('north', 'south', 'east', 'west')} | {
            'down': {'uv': [6, 6, 10, 10], 'texture': '#side', 'cullface': 'down'}}},
        {'from': [3, 9, 3], 'to': [13, 12, 13], 'faces': {
            f: {'uv': [3, 4, 13, 7], 'texture': '#side'} for f in ('north', 'south', 'east', 'west')} | {
            'up': {'uv': [3, 3, 13, 13], 'texture': '#top'}, 'down': {'uv': [3, 3, 13, 13], 'texture': '#side'}}},
        {'from': [7, 12, 7], 'to': [9, 14, 9], 'faces': {
            f: {'uv': [7, 1, 9, 3], 'texture': '#side'} for f in ('north', 'south', 'east', 'west')} | {
            'up': {'uv': [7, 7, 9, 9], 'texture': '#top'}}},
    ],
}


def modern(v):
    return v.startswith('26.')


def write(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + '\n')


def folder(v, kind):
    plural = {'loot_table': 'loot_tables', 'recipe': 'recipes', 'advancement': 'advancements', 'tags/block': 'tags/blocks'}
    return plural[kind] if v == '1.20.1' else kind


def ingredient(v, value):
    if modern(v):
        return value
    return {'tag': value[1:]} if value.startswith('#') else {'item': value}


def recipe(v, pattern, key, result):
    out = {'type': 'minecraft:crafting_shaped', 'category': 'misc'}
    out['pattern'] = pattern
    out['key'] = {k: ingredient(v, i) for k, i in key.items()}
    out['result'] = {'item': result, 'count': 1} if v == '1.20.1' else {'id': result, 'count': 1}
    return out


def unlock(v, recipe_id, item):
    if v == '1.20.1':
        predicate = {'tag': item[1:]} if item.startswith('#') else {'items': [item]}
    else:
        predicate = {'items': item}
    return {
        'parent': 'minecraft:recipes/root',
        'criteria': {
            'has_item': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [predicate]}},
            'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked',
                               'conditions': {('recipes' if v == '26.3' else 'recipe'): recipe_id}},
        },
        'requirements': [['has_item', 'has_the_recipe']],
        'rewards': {'recipes': [recipe_id]},
    }


def block_loot(v, block):
    if v == '26.3':
        pool = {'rolls': 1, 'condition': {'type': 'minecraft:survives_explosion'},
                'entries': [{'type': 'minecraft:item', 'name': block}]}
    else:
        pool = {'rolls': 1.0, 'conditions': [{'condition': 'minecraft:survives_explosion'}],
                'entries': [{'type': 'minecraft:item', 'name': block}]}
    return {'type': 'minecraft:block', 'random_sequence': block.replace(':', ':blocks/'), 'pools': [pool]}


def tag(v, values):
    entries = []
    for value in values:
        entries.append(value)
    return {'values': entries}


def generate(v):
    res = REPO / v / 'common' / 'src' / 'main' / 'resources'
    assets = res / 'assets' / NS
    data = res / 'data'
    for stale in (assets, data, res / 'resourcepacks', res / 'assets' / 'minecraft'):
        if stale.exists():
            shutil.rmtree(stale)

    write(res / 'pack.mcmeta', {'pack': {'description': 'Better Farming++ resources', **PACK[v]}})

    # Assets
    write(assets / 'lang' / 'en_us.json', LANG)
    write(assets / 'blockstates' / 'farming_crate.json', {'variants': {'': {'model': f'{NS}:block/farming_crate'}}})
    write(assets / 'blockstates' / 'sprinkler.json', {'variants': {
        'active=false': {'model': f'{NS}:block/sprinkler'}, 'active=true': {'model': f'{NS}:block/sprinkler'}}})
    write(assets / 'models' / 'block' / 'farming_crate.json',
          {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/crate'}})
    write(assets / 'models' / 'block' / 'sprinkler.json', SPRINKLER_MODEL)
    for item in ('farming_crate', 'sprinkler'):
        write(assets / 'models' / 'item' / f'{item}.json', {'parent': f'{NS}:block/{item}'})
        if modern(v):
            write(assets / 'items' / f'{item}.json', {'model': {'type': 'minecraft:model', 'model': f'{NS}:block/{item}'}})
    shutil.copytree(TOOLS / 'assets' / 'textures', assets / 'textures')
    shutil.copy(TOOLS / 'assets' / 'crate.png', assets / 'textures' / 'block' / 'crate.png')

    # The harvest indicators, as a built-in resource pack the loaders register.
    pack = res / 'resourcepacks' / 'harvest_indicators'
    write(pack / 'pack.mcmeta', {'pack': {'description': 'A marker above ripe crops (Better Farming++)', **INDICATOR_PACK_FORMAT[v]}})
    shutil.copytree(TOOLS / 'assets' / 'harvest_indicators', pack / 'assets' / 'minecraft')

    # Data
    for block in ('farming_crate', 'sprinkler'):
        write(data / NS / folder(v, 'loot_table') / 'blocks' / f'{block}.json', block_loot(v, f'{NS}:{block}'))
    for rid, (pattern, key, result, item) in RECIPES.items():
        write(data / NS / folder(v, 'recipe') / f'{rid}.json', recipe(v, pattern, key, result))
        write(data / NS / folder(v, 'advancement') / 'recipes' / 'misc' / f'{rid}.json', unlock(v, f'{NS}:{rid}', item))
    for tid, values in BLOCK_TAGS.items():
        ns, path = tid.split(':')
        write(data / ns / folder(v, 'tags/block') / f'{path}.json', tag(v, values))


def main():
    for v in VERSIONS:
        if (REPO / v).is_dir():
            generate(v)
            print('generated', v)


if __name__ == '__main__':
    main()
