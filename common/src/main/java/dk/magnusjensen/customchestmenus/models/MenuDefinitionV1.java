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

import java.util.List;
import java.util.Optional;

public record MenuDefinitionV1(int formatVersion,
                               String id,
                               String name,
                               MenuSize size,
                               Optional<MenuItem> filler,
                               List<MenuPage> pages) {

    public static final Codec<MenuDefinitionV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("format_version").forGetter(v -> v.formatVersion),
        Codec.STRING.fieldOf("id").forGetter(v -> v.id),
        Codec.STRING.fieldOf("name").forGetter(v -> v.name),
        MenuSize.CODEC.fieldOf("size").forGetter(v -> v.size),
        MenuItem.CODEC.optionalFieldOf("filler").forGetter(v -> v.filler),
        MenuPage.CODEC.listOf().fieldOf("pages").forGetter(v -> v.pages)
    ).apply(instance, (formatVersion, id, name, menuSize, menuItem, menuPages) -> {
        StringBuilder errors = new StringBuilder();

        var maxSlotsPerPage = menuSize.getSlots();

        for (var i = 0; i <menuPages.size(); i++) {
            var page = menuPages.get(i);
            var pageErrors = page.validateParsing(page.items(), menuSize);
            if (!pageErrors.isEmpty())
                errors.append("Page[").append(i).append("] errors:\n").append(pageErrors);

            if (page.items().size() > maxSlotsPerPage) {
                if (errors.isEmpty()) {
                    errors.append("Page[").append(i).append("] errors:\n");
                }
                errors.append("'").append(page.title()).append("' has ")
                    .append(page.items().size()).append(" items, which exceeds the maximum of ")
                    .append(maxSlotsPerPage).append(" for menu size ").append(menuSize).append("\n");
            }
        }

        if (!errors.isEmpty()) {
            throw new MenuValidationException("Validation errors:\n" + errors);
        }

        return new MenuDefinitionV1(formatVersion, id, name, menuSize, menuItem, menuPages);
    }));

    public MenuDefinition toMenuDefinition() {
        return new MenuDefinition(id, name, size, filler, pages);
    }
}
