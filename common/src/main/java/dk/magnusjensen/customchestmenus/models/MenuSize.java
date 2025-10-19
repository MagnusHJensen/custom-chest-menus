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

package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;

import java.util.Locale;

/**
 * Enum describing the size of the menu based on chest size, either SINGLE (3 rows) or DOUBLE (6 rows).
 */
public enum MenuSize {
    SINGLE(27),
    DOUBLE(54);

    public static final Codec<MenuSize> CODEC = Codec.STRING.xmap(
        s -> MenuSize.valueOf(s.toUpperCase(Locale.ROOT)),
        MenuSize::name
    );

    private final int slots;
    MenuSize(int slots) {
        this.slots = slots;
    }

    public int getSlots() {
        return slots;
    }

}
