# Custom Chest Menus - Just like you know them from plugin servers

![Screenshot of starter example custom chest menu first page](./docs/starter-custom-chest-menu.png)

Custom Chest Menus is a mod for Minecraft that allows server owners to create custom chest menus with various functionalities.
This mod is designed to enhance the gameplay experience by providing interactive and customizable interfaces for players.

## Features

Custom Chest Menus enables all server owners to create custom chest menus by writing it out in JSON files.

### Player
- Open any menu with a single command: `/ccm open <menu_id>`

### Server owners
- Open any menu for a given player with a single command: `/ccm open <menu_id> [player]` _(Requires permission level 2)_
- Create custom chest menus using JSON files
    - See [JSON Spec](docs/v1.schema.json) file for structure of JSON file.
    - Or use the [starter example menu](#starter-example).
- Reload all menus in-game through a command `/ccm reload` _(Requires permission level 2)_
- Currently supported actions:
    - Teleport to a location (also supports cross dimensions)
    - Craft Items
    - Run a command as player or server
        - Placeholders:
            - `%player%` will be replaced with the player's name.
            - `%uuid` will be replaced with the player's UUID.
        - _In the future more placeholders will be supported_
    - Close menu
    - Pagination
        - Next page
        - Previous page
        - Jump to page

## Roadmap

Check out the version [milestones](https://github.com/MagnusHJensen/custom-chest-menus/milestones)

## Guides

This section provides small, but meaningful guides/snippets for server owners to get started with Custom Chest Menus.

### Starter example

You can find the starter example JSON file [here](./docs/starter_example.json).



## Links

Join my [Discord](https://discord.gg/PHu8k32M3q) for support and updates! (Just created, so barebones)
- [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/custom-chest-menus)
- [Modrinth page](https://modrinth.com/mod/custom-chest-menus)