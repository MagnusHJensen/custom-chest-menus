# 2.4.0 - 1.21.1
- Added support for craft all by holding shift and clicking an item with `craft_items`.
- Added `missing_input_message` to the `craft_items` type, to allow custom missing input item message. It supports:
  - `%item%` - The missing item name.
  - `%missing%` - The amount that is missing.
  - `%required%` - The total amount that is required.
  - `%available%` - The amount the player currently has.