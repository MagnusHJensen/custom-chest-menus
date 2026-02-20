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
import dk.magnusjensen.customchestmenus.models.BaseItem;
import dk.magnusjensen.customchestmenus.models.MenuItem;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsActionV1;
import dk.magnusjensen.customchestmenus.models.actions.MenuAction;
import dk.magnusjensen.customchestmenus.models.actions.NoopAction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an item definition with a custom menu.
 */
public record MenuItemV1(
    int slot,
    ResourceLocation item,
    String name,
    int count,
    Optional<List<String>> lore,
    MenuAction action,
    Optional<CompoundTag> nbt,
    Map<DataComponentType<?>, Object> components
) {
    public static final Codec<MenuItemV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("slot").forGetter(MenuItemV1::slot),
        ResourceLocation.CODEC.fieldOf("item").forGetter(MenuItemV1::item),
        Codec.STRING.fieldOf("name").forGetter(MenuItemV1::name),
        Codec.INT.optionalFieldOf("count", 1).forGetter(MenuItemV1::count),
        Codec.STRING.listOf().optionalFieldOf("lore").forGetter(MenuItemV1::lore),
        MenuAction.CODEC_V1.optionalFieldOf("action", new NoopAction()).forGetter(MenuItemV1::action),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(MenuItemV1::nbt),
        DataComponentType.VALUE_MAP_CODEC.optionalFieldOf("components", Map.of()).forGetter(MenuItemV1::components)
    ).apply(instance, MenuItemV1::new));

    public BaseItem toBaseItem() {
        return new BaseItem(
            item,
            Component.literal(name),
            count,
            components
        );
    }

    public MenuItem toMenuItem() {
        var mergedComponents = new java.util.HashMap<>(components);

        var lore = this.lore.orElse(List.of());
        var itemLore = ItemLore.EMPTY;
        if (!lore.isEmpty()) {
            for (String line : lore) {
                itemLore = itemLore.withLineAdded(Component.literal(line));
            }
        }
        // Put the individual lore lines into the components map
        mergedComponents.put(DataComponents.LORE, itemLore);

        // Same for NBT
        nbt.ifPresent(data -> mergedComponents.put(DataComponents.CUSTOM_DATA, CustomData.of(data)));
        return new MenuItem(
            item,
            Component.literal(name),
            count,
            mergedComponents,
            slot,
            mapAction(action)
        );
    }

    private MenuAction mapAction(MenuAction action) {
        if (action instanceof CraftItemsActionV1 ca) {
            return ca.toUnified();
        }

        return action;
    }


}

