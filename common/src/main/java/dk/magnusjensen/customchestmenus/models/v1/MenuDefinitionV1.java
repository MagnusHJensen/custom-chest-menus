/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2025-2026  legenden (MagnusHJensen)
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

package dk.magnusjensen.customchestmenus.models.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.*;

import java.util.List;
import java.util.Optional;

public record MenuDefinitionV1(int formatVersion,
                               String id,
                               String name,
                               MenuSize size,
                               Optional<MenuItemV1> filler,
                               List<MenuPageV1> pages) {

    public static final Codec<MenuDefinitionV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("format_version").forGetter(v -> v.formatVersion),
        Codec.STRING.fieldOf("id").forGetter(v -> v.id),
        Codec.STRING.fieldOf("name").forGetter(v -> v.name),
        MenuSize.CODEC.fieldOf("size").forGetter(v -> v.size),
        MenuItemV1.CODEC.optionalFieldOf("filler").forGetter(v -> v.filler),
        MenuPageV1.CODEC.listOf().fieldOf("pages").forGetter(v -> v.pages)
    ).apply(instance, MenuDefinitionV1::new));

    public MenuDefinition toMenuDefinition() {
        return new MenuDefinition(id, name, size, MenuBackground.DEFAULT, filler.map(MenuItemV1::toBaseItem), pages.stream().map(MenuPageV1::toMenuPage).toList());
    }
}