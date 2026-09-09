# 2.4.0 - 26.2
- Added support for craft all by holding shift and clicking an item with `craft_items`.
- Added `missing_input_message` to the `craft_items` type, to allow custom missing input item message. It supports:
  - `%item%` - The missing item name.
  - `%missing%` - The amount that is missing.
  - `%required%` - The total amount that is required.
  - `%available%` - The amount the player currently has.

# 2.3.1 - 26.2
- Fixed an issue where `craft_items` action could not use more than 64 of a single item in its input.
