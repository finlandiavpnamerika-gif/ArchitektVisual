#!/usr/bin/env python3
"""Переименовывает intermediary-имена (class_123, method_456, field_789) в Yarn
во всех .java файлах из src/. Запускается в CI перед gradle build.

Использование: python3 tools/remap_yarn.py 1.21.11+build.3 [путь/к/mappings.tiny]
"""
import io
import re
import sys
import urllib.request
import zipfile
from pathlib import Path

ver = sys.argv[1] if len(sys.argv) > 1 else "1.21.11+build.3"

if len(sys.argv) > 2:
    text = Path(sys.argv[2]).read_text(encoding="utf-8")
else:
    url = f"https://maven.fabricmc.net/net/fabricmc/yarn/{ver}/yarn-{ver}-v2.jar"
    print("Downloading", url)
    data = urllib.request.urlopen(url).read()
    with zipfile.ZipFile(io.BytesIO(data)) as z:
        text = z.read("mappings/mappings.tiny").decode("utf-8")

cls_full = {}    # "net/minecraft/class_3675$class_307" -> "net/minecraft/client/util/InputUtil$Type"
cls_simple = {}  # "class_307" -> "Type"
members = {}     # "method_8035" -> "isAdvanced", "field_1668" -> "KEYSYM"

for line in text.splitlines()[1:]:
    p = line.split("\t")
    if p[0] == "c" and len(p) >= 3 and p[2]:
        inter, named = p[1], p[2]
        cls_full[inter] = named
        si = re.split(r"[/$]", inter)[-1]
        sn = re.split(r"[/$]", named)[-1]
        if si.startswith("class_") and sn != si:
            cls_simple[si] = sn
    elif p[0] == "" and len(p) >= 5 and p[1] in ("m", "f"):
        inter, named = p[3], p[4]
        if inter != named and named and re.fullmatch(r"(method|field)_\d+", inter):
            members[inter] = named

print(f"classes: {len(cls_full)}, members: {len(members)}")

RE_SLASH = re.compile(r"net/minecraft/(class_\d+(?:\$class_\d+)*)")
RE_DOT = re.compile(r"net\.minecraft\.(class_\d+(?:\.class_\d+)*)")
RE_BARE = re.compile(r"\bclass_\d+\b")
RE_MEMBER = re.compile(r"\b(?:method|field)_\d+\b")


def slash(m):
    full = "net/minecraft/" + m.group(1)
    return cls_full.get(full, m.group(0))


def dot(m):
    full = "net/minecraft/" + m.group(1).replace(".", "$")
    named = cls_full.get(full)
    return named.replace("/", ".").replace("$", ".") if named else m.group(0)


changed = 0
for f in Path("src").rglob("*.java"):
    s = f.read_text(encoding="utf-8")
    o = s
    s = RE_SLASH.sub(slash, s)
    s = RE_DOT.sub(dot, s)
    s = RE_BARE.sub(lambda m: cls_simple.get(m.group(0), m.group(0)), s)
    s = RE_MEMBER.sub(lambda m: members.get(m.group(0), m.group(0)), s)
    if s != o:
        f.write_text(s, encoding="utf-8")
        changed += 1
        print("remapped", f)

left = []
for f in Path("src").rglob("*.java"):
    for i, l in enumerate(f.read_text(encoding="utf-8").splitlines(), 1):
        if re.search(r"\b(class|method|field)_\d+\b", l):
            left.append(f"{f}:{i}: {l.strip()}")
print(f"files changed: {changed}, unmapped lines left: {len(left)}")
for l in left[:30]:
    print("  ", l)
