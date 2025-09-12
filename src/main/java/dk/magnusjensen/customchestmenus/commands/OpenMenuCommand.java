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

package dk.magnusjensen.customchestmenus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dk.magnusjensen.customchestmenus.CustomChestMenus;
import dk.magnusjensen.customchestmenus.commands.arguments.MenuArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class OpenMenuCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(CommandHandler.COMMAND_ROOT)
            .then(Commands.literal("open")
                .then(Commands.argument("menu", StringArgumentType.word())
                    .suggests(MenuArgument.MENU_IDS)
                    .executes(ctx -> {
                        var menuDef = MenuArgument.requireMenu(ctx, "menu");
                        var player = ctx.getSource().getPlayerOrException();
                        CustomChestMenus.openMenu(player, menuDef, 0);
                        return 1;
                    })
                )
            )
        );
    }
}
