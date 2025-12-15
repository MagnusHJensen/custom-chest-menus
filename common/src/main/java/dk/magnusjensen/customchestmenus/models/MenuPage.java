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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a single page within a custom menu.
 */
public record MenuPage(
    String title,
    List<MenuItem> items
) {
    public static final Codec<MenuPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("title").forGetter(MenuPage::title),
        MenuItem.CODEC.listOf().fieldOf("items").forGetter(MenuPage::items)
    ).apply(instance, MenuPage::new));

    public Component titleAsComponent() {
        return Component.literal(this.title);
    }



    String validateParsing(List<MenuItem> menuItems, MenuSize size) {
        StringBuilder errors = new StringBuilder();
        // Track if duplicate slots is used or out of bound slots
        var usedSlots = new HashMap<Integer, Boolean>();
        for (var menuItem : menuItems) {
            if (menuItem.slot() < 0 || menuItem.slot() >= size.getSlots()) {
                errors.append("Slot ").append(menuItem.slot()).append(" out of bounds (").append(0).append(", ").append(size.getSlots()).append(")\n");
            }
            if (usedSlots.getOrDefault(menuItem.slot(), false)) {
                errors.append("Slot ").append(menuItem.slot()).append(" already used\n");
            }
            usedSlots.put(menuItem.slot(), true);
        }

        return errors.toString();
    }
}
