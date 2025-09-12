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

package dk.magnusjensen.customchestmenus.commands.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.registries.CustomChestMenuRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MenuArgument {
    private MenuArgument() {}

    // Nice error if an ID is unknown
    private static final DynamicCommandExceptionType UNKNOWN_MENU =
        new DynamicCommandExceptionType(id -> Component.literal("Unknown menu id: " + id));

    /** Read the raw string argument (use with StringArgumentType.word()). */
    public static String getId(CommandContext<CommandSourceStack> ctx, String name) {
        return StringArgumentType.getString(ctx, name);
    }

    /** Resolve the menu or throw a command error. */
    public static MenuDefinition requireMenu(CommandContext<CommandSourceStack> ctx, String name)
        throws CommandSyntaxException {
        String id = getId(ctx, name);
        MenuDefinition def = CustomChestMenuRegistry.get(id);
        if (def == null) throw UNKNOWN_MENU.create(id);
        return def;
    }

    /** Suggestions for menu IDs from the active registry snapshot. */
    public static final SuggestionProvider<CommandSourceStack> MENU_IDS =
        (context, builder) -> {
            List<String> ids = CustomChestMenuRegistry.all().stream().map(MenuDefinition::id).toList();
            return SharedSuggestionProvider.suggest(ids, builder);
        };

}
