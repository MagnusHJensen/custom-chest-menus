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

package dk.magnusjensen.customchestmenus.models.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.MenuItem;
import dk.magnusjensen.customchestmenus.models.MenuPage;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Represents a single page within a custom menu.
 */
public record MenuPageV1(
    String title,
    List<MenuItemV1> items
) {
    public static final Codec<MenuPageV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("title").forGetter(MenuPageV1::title),
        MenuItemV1.CODEC.listOf().fieldOf("items").forGetter(MenuPageV1::items)
    ).apply(instance, MenuPageV1::new));

    public Component titleAsComponent() {
        return Component.literal(this.title);
    }

    public MenuPage toMenuPage() {
        List<MenuItem> mapped = this.items().stream().map(MenuItemV1::toMenuItem).toList();
        return new MenuPage(this.title, mapped);
    }
}
