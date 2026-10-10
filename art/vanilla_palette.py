#!/usr/bin/env python3
"""Generate GIMP/Aseprite .gpl palettes from vanilla Minecraft textures.

Uses only the Python standard library.

Examples:
    python art/vanilla_palette.py block/granite
    python art/vanilla_palette.py "block/*_planks" item/stick
    python art/vanilla_palette.py block/stone block/cobblestone --merge stones
    python art/vanilla_palette.py block/granite --jar path/to/client.jar

Texture names are paths under assets/minecraft/textures without ".png" and may
use * and ? wildcards. With no --jar or --dir the client jar for the version in
gradle.properties is downloaded once into art/.cache/.
"""

import argparse
import colorsys
import fnmatch
import json
import struct
import sys
import urllib.request
import zipfile
import zlib
from collections import Counter
from pathlib import Path

ART_DIR = Path(__file__).resolve().parent
ROOT = ART_DIR.parent
CACHE_DIR = ART_DIR / ".cache"
TEXTURE_PREFIX = "assets/minecraft/textures/"
MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"


# --- texture sources -------------------------------------------------------

def minecraft_version():
    for line in (ROOT / "gradle.properties").read_text().splitlines():
        key, _, value = line.partition("=")
        if key.strip() == "minecraft_version":
            return value.strip()
    sys.exit("minecraft_version not found in gradle.properties")


def fetch_json(url):
    with urllib.request.urlopen(url) as resp:
        return json.load(resp)


def client_jar(version):
    jar = CACHE_DIR / f"client-{version}.jar"
    if jar.exists():
        return jar
    manifest = fetch_json(MANIFEST_URL)
    entry = next((v for v in manifest["versions"] if v["id"] == version), None)
    if entry is None:
        sys.exit(f"Minecraft {version} not in Mojang's version manifest; pass --jar")
    url = fetch_json(entry["url"])["downloads"]["client"]["url"]
    print(f"Downloading Minecraft {version} client jar...", file=sys.stderr)
    CACHE_DIR.mkdir(exist_ok=True)
    tmp = jar.with_suffix(".part")
    urllib.request.urlretrieve(url, tmp)
    tmp.rename(jar)
    return jar


class JarSource:
    def __init__(self, path):
        self.zip = zipfile.ZipFile(path)
        self.names = [
            n[len(TEXTURE_PREFIX):-4]
            for n in self.zip.namelist()
            if n.startswith(TEXTURE_PREFIX) and n.endswith(".png")
        ]

    def read(self, name):
        return self.zip.read(f"{TEXTURE_PREFIX}{name}.png")


class DirSource:
    def __init__(self, path):
        self.root = Path(path)
        self.names = [p.relative_to(self.root).with_suffix("").as_posix()
                      for p in self.root.rglob("*.png")]

    def read(self, name):
        return (self.root / f"{name}.png").read_bytes()


# --- PNG decoding ----------------------------------------------------------

CHANNELS = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}


def decode_png(data):
    """Return a list of (r, g, b, a) pixels."""
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError("not a PNG")
    pos, idat, plte, trns = 8, b"", None, None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b"IHDR":
            width, height, depth, ctype, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif kind == b"PLTE":
            plte = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif kind == b"tRNS":
            trns = body
        elif kind == b"IDAT":
            idat += body
        elif kind == b"IEND":
            break
    if interlace:
        raise ValueError("interlaced PNGs are not supported")

    channels = CHANNELS[ctype]
    bits = channels * depth
    bpp = max(1, bits // 8)
    stride = (width * bits + 7) // 8
    raw = zlib.decompress(idat)

    rows, prev = [], bytearray(stride)
    for y in range(height):
        start = y * (stride + 1)
        ftype, line = raw[start], bytearray(raw[start + 1:start + 1 + stride])
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if ftype == 1:
                line[i] = (line[i] + a) & 0xFF
            elif ftype == 2:
                line[i] = (line[i] + b) & 0xFF
            elif ftype == 3:
                line[i] = (line[i] + (a + b) // 2) & 0xFF
            elif ftype == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 0xFF
        rows.append(line)
        prev = line

    pixels = []
    for line in rows:
        if depth < 8:
            mask = (1 << depth) - 1
            samples = [(line[i * depth // 8] >> (8 - depth - (i * depth) % 8)) & mask
                       for i in range(width)]
        else:
            step = depth // 8  # 16-bit samples keep the high byte
            samples = list(line[::step])
        for x in range(width):
            px = samples[x * channels:(x + 1) * channels]
            if ctype == 3:
                r, g, b = plte[px[0]]
                a = trns[px[0]] if trns and px[0] < len(trns) else 255
            elif ctype in (0, 4):
                v = px[0] * 255 // ((1 << depth) - 1) if depth < 8 else px[0]
                r = g = b = v
                a = px[1] if ctype == 4 else 255
            else:
                r, g, b = px[:3]
                a = px[3] if ctype == 6 else 255
            pixels.append((r, g, b, a))
    return pixels


# --- palette ---------------------------------------------------------------

def luma(rgb):
    r, g, b = rgb
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


SORTS = {
    "luma": lambda item: luma(item[0]),
    "hue": lambda item: (colorsys.rgb_to_hsv(*(c / 255 for c in item[0]))[0], luma(item[0])),
    "count": lambda item: -item[1],
}


def collect(source, names, alpha_min):
    counts = Counter()
    for name in names:
        for r, g, b, a in decode_png(source.read(name)):
            if a >= alpha_min:
                counts[(r, g, b)] += 1
    return counts


def write_gpl(path, title, counts, sort, columns):
    items = sorted(counts.items(), key=SORTS[sort])
    lines = ["GIMP Palette", f"Name: {title}", f"Columns: {columns}", "#"]
    for (r, g, b), n in items:
        lines.append(f"{r:3d} {g:3d} {b:3d}\t#{r:02x}{g:02x}{b:02x} ({n}px)")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n")
    print(f"{path.relative_to(Path.cwd()) if path.is_relative_to(Path.cwd()) else path}: "
          f"{len(items)} colours")


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("textures", nargs="+",
                    help="texture paths like block/granite; wildcards allowed")
    src = ap.add_mutually_exclusive_group()
    src.add_argument("--jar", help="Minecraft client jar to read from")
    src.add_argument("--dir", help="folder of extracted textures (assets/minecraft/textures)")
    ap.add_argument("--version", help="Minecraft version to download (default: gradle.properties)")
    ap.add_argument("--merge", metavar="NAME",
                    help="write one combined palette NAME.gpl instead of one per texture")
    ap.add_argument("--out", default=str(ART_DIR / "palettes"), help="output folder")
    ap.add_argument("--sort", choices=SORTS, default="luma", help="colour order (default: luma)")
    ap.add_argument("--columns", type=int, default=8, help="swatch columns (default: 8)")
    ap.add_argument("--alpha-min", type=int, default=1,
                    help="skip pixels with alpha below this (default: 1, i.e. only fully clear)")
    args = ap.parse_args()

    if args.dir:
        source = DirSource(args.dir)
    else:
        source = JarSource(args.jar or client_jar(args.version or minecraft_version()))

    names = []
    for pattern in args.textures:
        pattern = pattern.removesuffix(".png")
        matched = sorted(n for n in source.names if fnmatch.fnmatchcase(n, pattern))
        if not matched:
            sys.exit(f"no texture matches '{pattern}'")
        names += [n for n in matched if n not in names]

    out = Path(args.out)
    if args.merge:
        write_gpl(out / f"{args.merge}.gpl", args.merge,
                  collect(source, names, args.alpha_min), args.sort, args.columns)
    else:
        for name in names:
            title = name.split("/")[-1]
            write_gpl(out / f"{title}.gpl", title,
                      collect(source, [name], args.alpha_min), args.sort, args.columns)


if __name__ == "__main__":
    main()
