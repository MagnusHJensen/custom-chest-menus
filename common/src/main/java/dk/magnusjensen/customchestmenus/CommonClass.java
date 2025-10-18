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

import com.mojang.brigadier.CommandDispatcher;
import dk.magnusjensen.customchestmenus.commands.CommandHandler;
import dk.magnusjensen.customchestmenus.registry.CustomChestMenuRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;


public class CommonClass {

    public static void init() {
        // Move init code here
    }

    public static void loadMenus(MinecraftServer server) {
        CustomChestMenuRegistry.loadMenus(server);
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandHandler.register(dispatcher);
    }
}
