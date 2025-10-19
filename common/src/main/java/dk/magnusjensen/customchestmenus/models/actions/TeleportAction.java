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

import java.util.Optional;

public record TeleportAction(
    double x,
    double y,
    double z,
    Optional<String> dimension
) implements MenuAction {

    public static final MapCodec<TeleportAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.DOUBLE.fieldOf("x").forGetter(TeleportAction::x),
        Codec.DOUBLE.fieldOf("y").forGetter(TeleportAction::y),
        Codec.DOUBLE.fieldOf("z").forGetter(TeleportAction::z),
        Codec.STRING.optionalFieldOf("dimension").forGetter(TeleportAction::dimension)
    ).apply(i, TeleportAction::new));

    @Override
    public MenuActionType type() {
        return MenuActionType.TELEPORT;
    }
}
