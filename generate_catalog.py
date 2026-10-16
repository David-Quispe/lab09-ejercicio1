"""Create a compact catalog index from PokéAPI's public source CSV files."""

import csv
import io
import json
import urllib.request
from pathlib import Path


BASE = "https://raw.githubusercontent.com/PokeAPI/pokeapi/master/data/v2/csv/"
OUT = Path(__file__).parent / "app/src/main/assets/pokemon_index.json"
CHART_OUT = Path(__file__).parent / "app/src/main/assets/type_chart.json"


def rows(name):
    with urllib.request.urlopen(BASE + name, timeout=30) as response:
        data = response.read().decode("utf-8-sig")
    return list(csv.DictReader(io.StringIO(data)))


types = {int(row["id"]): row["identifier"] for row in rows("types.csv")}
egg_groups = {int(row["id"]): row["identifier"] for row in rows("egg_groups.csv")}
species = {int(row["id"]): row for row in rows("pokemon_species.csv")}
pokemon_types = {}
for row in rows("pokemon_types.csv"):
    pokemon_types.setdefault(int(row["pokemon_id"]), []).append((int(row["slot"]), types[int(row["type_id"])]))
pokemon_eggs = {}
for row in rows("pokemon_egg_groups.csv"):
    pokemon_eggs.setdefault(int(row["species_id"]), []).append(egg_groups[int(row["egg_group_id"])] )
pokemon_stats = {}
for row in rows("pokemon_stats.csv"):
    pokemon_stats.setdefault(int(row["pokemon_id"]), {})[int(row["stat_id"])] = int(row["base_stat"])

result = []
for row in rows("pokemon.csv"):
    pokemon_id = int(row["id"])
    species_id = int(row["species_id"])
    info = species.get(species_id, {})
    stats = pokemon_stats.get(pokemon_id, {})
    result.append({
        "id": pokemon_id,
        "species": species_id,
        "default": row["is_default"] == "1",
        "generation": int(info.get("generation_id") or 0),
        "types": [type_name for _, type_name in sorted(pokemon_types.get(pokemon_id, []))],
        "eggs": pokemon_eggs.get(species_id, []),
        "height": int(row["height"]),
        "weight": int(row["weight"]),
        "attack": stats.get(2, 0),
        "total": sum(stats.values()),
    })

OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(result, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
print(f"{len(result)} entries -> {OUT} ({OUT.stat().st_size} bytes)")

api_data = "https://raw.githubusercontent.com/PokeAPI/api-data/master/data/api/v2/type/"
chart = {}
for type_id in range(1, 19):
    with urllib.request.urlopen(f"{api_data}{type_id}/index.json", timeout=30) as response:
        item = json.load(response)
    relations = item["damage_relations"]
    multipliers = {name: 1.0 for name in types.values() if name not in ("unknown", "shadow")}
    for key, value in (("double_damage_to", 2.0), ("half_damage_to", 0.5), ("no_damage_to", 0.0)):
        for target in relations[key]:
            multipliers[target["name"]] = value
    chart[item["name"]] = multipliers
CHART_OUT.write_text(json.dumps(chart, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
print(f"{len(chart)} types -> {CHART_OUT} ({CHART_OUT.stat().st_size} bytes)")
