# Purradox

Purradox cat types are data driven. One `purradox:cat` entity can become any
cat declared by a datapack.

## Adding a cat type

Create:

`data/<your_namespace>/purradox/cat_type/<cat_name>.json`

The cat type ID is `<your_namespace>:<cat_name>`. Definitions are loaded as a
synced datapack registry, so the server owns gameplay values and sends them to
clients when they connect. `/reload` reloads the registry. Existing cats retain
their type ID and immediately use the updated definition.

A complete example:

```json
{
  "translation_key": "cat_type.example.ruby",
  "texture": "default",
  "overlay": "example:textures/entity/cat/ruby_overlay.png",
  "food": "#minecraft:cat_food",
  "stats": {
    "max_health": 14.0,
    "movement_speed": 0.32,
    "attack_damage": 4.0,
    "scale": 1.0
  },
  "taming_chance": 0.25,
  "attachments": [
    {
      "model": "example:ruby_backpack",
      "anchor": "body",
      "visible_when": "tamed",
      "transform": {
        "translation": { "x": 0.0, "y": -0.1, "z": 0.0 },
        "rotation": { "x": 0.0, "y": 0.0, "z": 0.0 },
        "scale": { "x": 0.5, "y": 0.5, "z": 0.5 }
      }
    }
  ],
  "production": {
    "interval": {
      "min_ticks": 1200,
      "max_ticks": 2400
    },
    "rolls": 1,
    "conditions": {
      "requires_tamed": true,
      "requires_adult": true,
      "requires_sitting": false,
      "requires_on_ground": true,
      "dimensions": ["minecraft:overworld"]
    },
    "outputs": [
      {
        "stack": {
          "id": "minecraft:redstone",
          "count": 2
        },
        "weight": 9
      },
      {
        "stack": {
          "id": "minecraft:diamond",
          "count": 1
        },
        "weight": 1
      }
    ]
  },
  "breeding": {
    "offspring": [
      { "type": "example:ruby", "weight": 3 },
      { "type": "purradox:tabby", "weight": 1 }
    ]
  }
}
```

`food` is a Minecraft ingredient and accepts an item, tag, or ingredient list.
Item stacks in `outputs` support normal data components.

`texture` accepts a texture ID or the special value `default`. With `default`,
each spawned cat uses its randomly selected vanilla cat texture; bred cats
inherit one parent's vanilla texture. Omitting `texture` has the same effect.
An optional `overlay` texture is rendered over the base texture and follows the
same model animations. Overlay images should use transparency and the vanilla
cat texture layout.

### Attachment models

Attachment `model` values are item-model definition IDs from a resource pack.
For `example:ruby_backpack`, create
`assets/example/items/ruby_backpack.json`; that definition can reference normal
geometry under `assets/example/models/item/`. The attachment is rendered
without requiring a real registered item.

Supported anchors:

- `root`
- `head`
- `body`
- `tail_1`
- `tail_2`
- `left_front_leg` / `right_front_leg`
- `left_hind_leg` / `right_hind_leg`

Supported visibility values are `always`, `tamed`, `untamed`, `adult`, `baby`,
and `sitting`. Translation uses blocks, rotation uses degrees, and scale is
per-axis.

### Spawning a configured type

The Spawn Eggs creative tab automatically contains one named egg for every cat
type in the current datapacks. Adding a cat definition therefore also adds its
spawn egg variant without registering another item.


## Validation and behavior

- Invalid ranges and empty production output lists fail datapack loading with a
  useful codec error.
- Production cooldown is saved on the cat and pauses while its configured
  conditions are not met.
- Weighted output rolls create copies of the configured stacks.
- Cat type, textures, attachment definitions, food, stats, production, and
  breeding outcomes all sync to multiplayer clients.
- If a saved type disappears, the entity remains loadable and uses the first
  available cat definition until its ID becomes available again.
