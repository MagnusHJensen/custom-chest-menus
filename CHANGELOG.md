# 1.4.0 - 1.21.10 (+.9 Fabric)
- Added interactivity for Custom Chest Menus
  - `/ccm bind <menu_id>` Starts binding process, where if a block or entity is right-clicked it will bind the menu to the block/entity.
    - If a user right-clicks this block/entity it opens the menu.
    - `/ccm stop-binding` to exit binding mode.
    - `/ccm unbind` to start unbinding blocks or entities, where if a bound block/entity is right-clicked it will unbind the menu from the block/entity.
  - `/ccm list-bindings` Shows a chat list of all bound blocks and entities and a text element to teleport to it.
  - `/ccm bind-overlay` Toggles an overlay, which draws boxes around blocks and entities that are bound to a menu if within 64 blocks of it.