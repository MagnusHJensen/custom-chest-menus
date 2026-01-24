# 2.0.0 - 1.21.11
- `name` can now be left blank to default to the Item's original name.
- `action` under a menu item can now be left blank, and it will default to the `noop` action.
- `craft_items` action, now has an additional field `hide_text` which hides the built-in crafting lore text (Inputs, Outputs text). Defaults to **false**
- If no filler item is set, then an empty item stack is placed as filler (basically disabling the slots with emptiness)
- `background` can now be set to show a custom background texture, with support for setting menu title location and hiding player inventory.

For breaking changes from 1.x to 2.0: Check out the [migrations guide](https://github.com/MagnusHJensen/custom-chest-menus/wiki/Migrations)