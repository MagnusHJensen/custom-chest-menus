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

package dk.magnusjensen.customchestmenus;

import dk.magnusjensen.customchestmenus.client.screen.CustomChestScreen;
import dk.magnusjensen.customchestmenus.network.FabricClientNetwork;
import dk.magnusjensen.customchestmenus.registry.FabricMenuRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.mixin.client.rendering.WorldRendererMixin;
import net.minecraft.client.gui.screens.MenuScreens;

public class FabricClientCustomChestMenus implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientNetwork.register();

        MenuScreens.register(FabricMenuRegistry.CUSTOM_CHEST_MENU, CustomChestScreen::new);
    }
}
