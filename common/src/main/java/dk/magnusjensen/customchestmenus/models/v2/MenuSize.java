/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2026  legenden (MagnusHJensen)
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

package dk.magnusjensen.customchestmenus.models.v2;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.MenuSizeType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class MenuSize {
    private static final Codec<MenuSize> FROM_STRING = Codec.STRING.xmap(
        s -> {
            var sizeType = MenuSizeType.valueOf(s.toUpperCase());
            return new MenuSize(sizeType.getRows(), sizeType.getColumns(), true);
        },
        menuSize -> {
            if (!menuSize.isShorthand()) {
                throw new IllegalArgumentException("MenuSize was not created via shorthand syntax, cannot convert to string");
            }
            if (menuSize.getRows() == 3 && menuSize.getColumns() == 9) {
                return MenuSizeType.SINGLE.toString();
            } else if (menuSize.getRows() == 6 && menuSize.getColumns() == 9) {
                return MenuSizeType.DOUBLE.toString();
            } else {
                throw new IllegalArgumentException("Invalid menu size: " + menuSize.getRows() + " rows, " + menuSize.getColumns() + " columns");
            }
        });

    private static final Codec<MenuSize> FROM_OBJECT = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("rows").forGetter(MenuSize::getRows),
        Codec.INT.fieldOf("columns").forGetter(MenuSize::getColumns)
    ).apply(i, MenuSize::new));

    public static final Codec<MenuSize> CODEC = Codec.xor(FROM_STRING, FROM_OBJECT)
        .xmap(
            e -> e.map(l -> l, r -> r),            // Either<MenuSize, MenuSize> -> MenuSize
            size -> size.isShorthand()             // pick which side to encode
                ? Either.left(size)
                : Either.right(size));

    public static final StreamCodec<ByteBuf, MenuSize> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, MenuSize::getRows,
        ByteBufCodecs.VAR_INT, MenuSize::getColumns,
        ByteBufCodecs.BOOL,    MenuSize::isShorthand,
        MenuSize::new          // (int, int, boolean) constructor
    );

    private final int rows;
    private final int columns;
    private boolean isShorthand; // Indicates whether it was created via shorthand syntax or not.

    public MenuSize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public MenuSize(int rows, int columns, boolean isShorthand) {
        this(rows, columns);
        this.isShorthand = isShorthand;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public boolean isShorthand() {
        return isShorthand;
    }

    public int slotCount() {
        return rows * columns;
    }
}
