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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record CommandAction(String command, Optional<Boolean> runAsPlayer) implements MenuAction {

    public static final Codec<String> NON_BLANK =
        Codec.STRING.flatXmap(
            s -> (s != null && !s.isBlank())
                ? DataResult.success(s)
                : DataResult.error(() -> "String must be non-blank"),
            DataResult::success // encoder path (value -> ok)
        );

    public static final Codec<CommandAction> CODEC = RecordCodecBuilder.create(i -> i.group(
        NON_BLANK.fieldOf("command").forGetter(CommandAction::command),
        Codec.BOOL.optionalFieldOf("run_as_player").forGetter(CommandAction::runAsPlayer)
    ).apply(i, CommandAction::new));

    @Override
    public MenuActionType type() {
        return MenuActionType.COMMAND;
    }

    public boolean shouldRunAsPlayer() {
        return runAsPlayer.orElse(false);
    }
}
