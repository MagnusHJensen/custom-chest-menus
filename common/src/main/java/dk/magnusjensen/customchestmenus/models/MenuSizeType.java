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
public enum MenuSizeType {
    SINGLE,
    DOUBLE;

    public static final Codec<MenuSizeType> CODEC = Codec.STRING.xmap(
        s -> MenuSizeType.valueOf(s.toUpperCase(Locale.ROOT)),
        MenuSizeType::name
    );

    private static final int SINGLE_ROWS = 3;
    private static final int DOUBLE_ROWS = 6;
    private static final int COLUMNS = 9;

    public int getRows() {
        return this == SINGLE ? SINGLE_ROWS : DOUBLE_ROWS;
    }

    public int getColumns() {
        return COLUMNS;
    }
}
