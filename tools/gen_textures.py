#!/usr/bin/env python3
"""Draws the mod's own textures (no vanilla art is copied) into tools/assets/:

  textures/block/sprinkler.png        copper for the sprinkler's pipe and head
  textures/block/sprinkler_top.png    the head's top, with nozzle holes
  textures/gui/container/farming_crate.png   the crate screen (176x178) and its status lamps at u=176

  python3 tools/gen_textures.py     (then tools/datagen.py copies them into every workspace)
"""
from __future__ import annotations

import random
from pathlib import Path

from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parent / 'assets' / 'textures'

COPPER = [(0xB4, 0x5A, 0x36), (0xC8, 0x6B, 0x41), (0xD9, 0x7E, 0x4E), (0xE8, 0x95, 0x62)]
COPPER_DARK = (0x8A, 0x42, 0x28)
COPPER_LIGHT = (0xF4, 0xB0, 0x80)
PATINA = (0x5F, 0xA8, 0x8C)


def copper(seed: int, top: bool = False) -> Image.Image:
    rnd = random.Random(seed)
    im = Image.new('RGBA', (16, 16))
    px = im.load()
    for y in range(16):
        for x in range(16):
            c = COPPER[min(3, max(0, int(rnd.gauss(1.6, 0.8))))]
            px[x, y] = (*c, 255)
    # Rivets/banding: a darker band every 5 pixels and a highlight on the top-left edges.
    for x in range(16):
        px[x, 0] = (*COPPER_LIGHT, 255)
        px[x, 15] = (*COPPER_DARK, 255)
    for y in range(16):
        px[0, y] = (*COPPER_LIGHT, 255)
        px[15, y] = (*COPPER_DARK, 255)
    if not top:
        for y in (5, 10):
            for x in range(1, 15):
                px[x, y] = (*COPPER_DARK, 255)
        for (x, y) in ((3, 2), (12, 13), (7, 8)):
            px[x, y] = (*PATINA, 255)
    else:
        # Nozzle holes in a ring, a centre nozzle.
        for (x, y) in ((4, 4), (11, 4), (4, 11), (11, 11), (7, 3), (3, 7), (12, 8), (8, 12)):
            px[x, y] = (0x3A, 0x22, 0x18, 255)
            px[x + 1 if x < 15 else x, y] = (0x55, 0x30, 0x20, 255)
        for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
            px[x, y] = (*COPPER_LIGHT, 255)
    return im


def bevel(d: ImageDraw.ImageDraw, x0, y0, x1, y1, light, dark, fill):
    d.rectangle([x0, y0, x1, y1], fill=fill)
    d.line([x0, y0, x1, y0], fill=light)
    d.line([x0, y0, x0, y1], fill=light)
    d.line([x0, y1, x1, y1], fill=dark)
    d.line([x1, y0, x1, y1], fill=dark)


def slot(d, x, y):
    # 18x18 recessed slot at (x, y): dark top/left, light bottom/right.
    d.rectangle([x, y, x + 17, y + 17], fill=(0x8B, 0x8B, 0x8B))
    d.line([x, y, x + 16, y], fill=(0x37, 0x37, 0x37))
    d.line([x, y, x, y + 16], fill=(0x37, 0x37, 0x37))
    d.line([x + 1, y + 17, x + 17, y + 17], fill=(0xFF, 0xFF, 0xFF))
    d.line([x + 17, y + 1, x + 17, y + 17], fill=(0xFF, 0xFF, 0xFF))


def gui() -> Image.Image:
    im = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    w, h = 176, 178
    # Panel with the usual rounded black outline, white highlight and grey shadow.
    d.rectangle([1, 1, w - 2, h - 2], fill=(0xC6, 0xC6, 0xC6))
    d.line([2, 0, w - 4, 0], fill=(0, 0, 0))
    d.line([0, 2, 0, h - 4], fill=(0, 0, 0))
    d.line([3, h - 1, w - 3, h - 1], fill=(0, 0, 0))
    d.line([w - 1, 3, w - 1, h - 3], fill=(0, 0, 0))
    for (x, y) in ((1, 1), (w - 3, 1), (1, h - 3), (w - 2, h - 2), (w - 3, h - 2), (w - 2, h - 3), (2, h - 2), (w - 2, 2)):
        d.point((x, y), fill=(0, 0, 0))
    d.line([2, 1, w - 4, 1], fill=(0xFF, 0xFF, 0xFF))
    d.line([1, 2, 1, h - 4], fill=(0xFF, 0xFF, 0xFF))
    d.line([2, 2, w - 5, 2], fill=(0xFF, 0xFF, 0xFF))
    d.line([2, 2, 2, h - 5], fill=(0xFF, 0xFF, 0xFF))
    d.line([3, h - 2, w - 3, h - 2], fill=(0x55, 0x55, 0x55))
    d.line([w - 2, 3, w - 2, h - 3], fill=(0x55, 0x55, 0x55))
    d.line([4, h - 3, w - 3, h - 3], fill=(0x55, 0x55, 0x55))
    d.line([w - 3, 4, w - 3, h - 3], fill=(0x55, 0x55, 0x55))
    # A wheat-green accent under the title.
    d.line([8, 15, w - 9, 15], fill=(0x8F, 0xB0, 0x4E))
    for row in range(3):
        for col in range(9):
            slot(d, 7 + col * 18, 17 + row * 18)
    # Status strip (lamp + text) between the crate and the inventory.
    bevel(d, 7, 72, w - 8, 84, (0x8B, 0x8B, 0x8B), (0xFF, 0xFF, 0xFF), (0xB8, 0xB8, 0xB8))
    for row in range(3):
        for col in range(9):
            slot(d, 7 + col * 18, 95 + row * 18)
    for col in range(9):
        slot(d, 7 + col * 18, 153)
    # Lamps: green (working), red (full), grey (idle).
    for i, (core, rim) in enumerate((((0x6C, 0xE0, 0x4A), (0x2E, 0x6B, 0x1E)), ((0xF0, 0x50, 0x40), (0x7A, 0x1E, 0x16)),
                                     ((0x9A, 0x9A, 0x9A), (0x55, 0x55, 0x55)))):
        y = i * 8
        d.ellipse([176, y, 183, y + 7], fill=rim)
        d.ellipse([177, y + 1, 182, y + 6], fill=core)
        d.point((178, y + 2), fill=(0xFF, 0xFF, 0xFF))
    return im


def main():
    (OUT / 'block').mkdir(parents=True, exist_ok=True)
    (OUT / 'gui' / 'container').mkdir(parents=True, exist_ok=True)
    copper(11).save(OUT / 'block' / 'sprinkler.png')
    copper(23, top=True).save(OUT / 'block' / 'sprinkler_top.png')
    gui().save(OUT / 'gui' / 'container' / 'farming_crate.png')
    print('textures written to', OUT)


if __name__ == '__main__':
    main()
