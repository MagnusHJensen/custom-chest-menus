/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2025  legenden (MagnusHJensen)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dk.magnusjensen.customchestmenus.registry;

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class FabricMenuRegistry {
    public static final MenuType<CustomChestMenu> CUSTOM_CHEST_MENU = Registry.register(
        BuiltInRegistries.MENU,
        Identifier.tryBuild(Constants.MOD_ID, "custom_chest_menu"),
        new ExtendedScreenHandlerType<>(CustomChestMenu::new, CustomChestMenu.Payload.STREAM_CODEC) // factory: (syncId, playerInv) -> new MyMenu(syncId, playerInv)
    );

    public static void register() {
        Constants.LOGGER.info("Registering menus for Fabric.");
    }
}
