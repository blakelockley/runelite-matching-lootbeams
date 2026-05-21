# Matching Lootbeams

A RuneLite plugin that colors ground-item loot beams using each item sprite's dominant color.

The built-in Ground Items plugin colors loot beams by value tier — every "high value" drop looks the same. This plugin replaces that with a per-item beam color derived from the item's sprite, so a stack of coins shines gold, a dragon scimitar pulses red, etc.

## How it works

1. On every `ItemSpawned` event, the plugin extracts the item's sprite via `ItemManager.getImage()`.
2. Pixels are quantized into a 4-bit-per-channel histogram, weighted by saturation so neutral grays don't dominate.
3. The centroid of the heaviest bucket becomes the beam color.
4. A `RuneLiteObject` is spawned at the tile with a recolored copy of the modern loot beam model (id `43330`) — the same model the built-in plugin uses, with the same lighting and `FX_BEAM_IDLE` animation.

## Setup with the built-in Ground Items plugin

This plugin runs *alongside* Ground Items. To avoid duplicate beams:

1. Keep **Ground Items** enabled (it still handles text labels, value tiers, and the hide/highlight lists).
2. In Ground Items' config, set **Loot beam tier** to **None** / off.
3. Enable **Matching Lootbeams**.

## Config

| Option | Default | Purpose |
|---|---|---|
| **Show beam** | on | Master toggle for the 3D beam. |
| **Min value** | 0 | Skip items whose stack value is below this. Per-item value is `max(shop value, GE price)` multiplied by the stack quantity. |
| **Fallback color** | white | Used when no dominant color can be extracted (monochrome sprites or load failure). |

## Credits

The `Lootbeam.java` class is a verbatim copy of the equivalent class in the [RuneLite](https://github.com/runelite/runelite) repo (BSD 2-Clause, © 2021 Trevor). License header preserved.

## License

BSD 2-Clause. See [LICENSE](LICENSE).
