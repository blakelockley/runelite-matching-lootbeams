# Matching Lootbeams

![Matching Lootbeams in action](https://raw.githubusercontent.com/blakelockley/runelite-matching-lootbeams/main/screenshots/zenyte-serp-helm.png)

Loot beams colored by each item's dominant sprite color — so coins shine gold, a dragon scimitar pulses red, and you can tell drops apart at a glance.

## Setup

This plugin runs alongside RuneLite's built-in **Ground Items** plugin. To avoid duplicate beams:

1. Keep **Ground Items** enabled — it still handles text labels, value tiers, and the hide/highlight lists.
2. In Ground Items' config, set **Loot beam tier** to **None**.
3. Enable **Matching Lootbeams**.

## Config

| Option | Default | Purpose |
|---|---|---|
| **Show beam** | on | Master toggle for the colored beam. |
| **Min value** | 0 | Skip items whose stack value is below this. Per-item value is `max(shop value, GE price)`, multiplied by the stack quantity. |
| **Fallback color** | white | Used when no dominant color can be extracted (e.g. monochrome sprites). |

## How it works

Each spawned item's sprite is sampled into a coarse RGB histogram (16 buckets per channel), weighted by saturation so neutral grays don't dominate. The heaviest bucket's centroid becomes the beam color. A `RuneLiteObject` is then placed at the tile with a recolored copy of RuneLite's modern loot beam model (id `43330`) — same model, same `FX_BEAM_IDLE` animation, just a different color per item.

## Credits

The `Lootbeam.java` class is a verbatim copy of the equivalent class in the [RuneLite](https://github.com/runelite/runelite) repo (BSD 2-Clause, © 2021 Trevor). License header preserved.

## License

BSD 2-Clause. See [LICENSE](LICENSE).

## TODOs

- Clear cache when changing minimum filter
- Add options for different sampling methods
- Investigate why a lot of item show a pinkish loot beam
- Update default minimum value to be 100,000
- Add filter to only apply to the players dropped items
