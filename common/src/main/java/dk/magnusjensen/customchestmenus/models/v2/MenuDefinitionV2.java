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

package dk.magnusjensen.customchestmenus.models.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.*;

import java.util.List;
import java.util.Optional;

public record MenuDefinitionV2(int formatVersion,
                               String id,
                               String name,
                               MenuSize size,
                               MenuBackground background,
                               Optional<BaseItem> filler,
                               List<MenuPage> pages) {

    public static final Codec<MenuDefinitionV2> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("format_version").forGetter(v -> v.formatVersion),
        Codec.STRING.fieldOf("id").forGetter(v -> v.id),
        Codec.STRING.fieldOf("name").forGetter(v -> v.name),
        MenuSize.CODEC.fieldOf("size").forGetter(v -> v.size),
        MenuBackground.CODEC.optionalFieldOf("background", MenuBackground.DEFAULT).forGetter(v -> v.background),
        BaseItem.CODEC.optionalFieldOf("filler").forGetter(v -> v.filler),
        MenuPage.CODEC.listOf().fieldOf("pages").forGetter(v -> v.pages)
    ).apply(instance, MenuDefinitionV2::new));

    public MenuDefinition toMenuDefinition() {
        return new MenuDefinition(id, name, size, background, filler, pages);
    }
}