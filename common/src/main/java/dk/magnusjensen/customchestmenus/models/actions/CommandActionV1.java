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

package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.MenuValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dk.magnusjensen.customchestmenus.models.actions.CommandAction.NON_BLANK;

public record CommandActionV1(List<String> commands, Optional<Boolean> runAsPlayer) implements MenuAction {


    public static final MapCodec<CommandActionV1> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        NON_BLANK.optionalFieldOf("command").forGetter(ignored -> Optional.empty()), // backwards compat
        NON_BLANK.listOf().optionalFieldOf("commands").forGetter(ignored -> Optional.empty()),
        Codec.BOOL.optionalFieldOf("run_as_player").forGetter(CommandActionV1::runAsPlayer)
    ).apply(i, (commandOpt, commandsOpt, runAsPlayer) -> {
        // Validation logic
        if (commandOpt.isPresent() && commandsOpt.isPresent()) {
            throw new MenuValidationException("Cannot have both 'command' and 'commands' fields set");
        }
        if (commandOpt.isEmpty() && commandsOpt.isEmpty()) {
            throw new MenuValidationException("Either 'command' or 'commands' field must be present");
        }

        // Combine fields into a single list
        List<String> commands = new ArrayList<>();
        commandOpt.ifPresent(commands::add);
        commandsOpt.ifPresent(commands::addAll);

        return new CommandActionV1(commands, runAsPlayer);
    }));

    public boolean shouldRunAsPlayer() {
        return runAsPlayer.orElse(false);
    }

    public CommandAction toUnified() {
        return new CommandAction(commands, runAsPlayer);
    }

    @Override
    public MenuActionTypeUnified type() {
        return null;
    }

    @Override
    public MenuActionTypeV1 typeV1() {
        return MenuActionTypeV1.COMMAND;
    }
}
