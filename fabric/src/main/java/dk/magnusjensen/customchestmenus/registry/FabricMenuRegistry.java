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
import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FabricMenuRegistry {


    public static final ExtendedMenuType<CustomChestMenu, CustomChestMenu.Payload> CUSTOM_CHEST_MENU = register(
        Utils.modLoc("custom_chest_menu"),
        CustomChestMenu::new,
        CustomChestMenu.Payload.STREAM_CODEC
    );

    public static <T extends AbstractContainerMenu, D> ExtendedMenuType<T, D> register(
        Identifier name,
        ExtendedMenuType.ExtendedFactory<T, D> supplier,
        StreamCodec<RegistryFriendlyByteBuf, D> dataCodec
    ) {
        return Registry.register(BuiltInRegistries.MENU, name, new ExtendedMenuType<>(supplier, dataCodec));
    }

    public static void register() {
        Constants.LOGGER.info("Registering menus for Fabric.");
    }
}
