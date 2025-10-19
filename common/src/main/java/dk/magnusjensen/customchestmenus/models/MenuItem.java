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
import dk.magnusjensen.customchestmenus.models.actions.CraftItem;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction;
import dk.magnusjensen.customchestmenus.models.actions.MenuAction;
import dk.magnusjensen.customchestmenus.models.actions.NoopAction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an item definition with a custom menu.
 */
public record MenuItem(
    int slot,
    ResourceLocation item,
    String name,
    Optional<List<String>> lore,
    MenuAction action
) {
    public static final Codec<MenuItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("slot").forGetter(MenuItem::slot),
        ResourceLocation.CODEC.fieldOf("item").forGetter(MenuItem::item),
        Codec.STRING.fieldOf("name").forGetter(MenuItem::name),
        Codec.STRING.listOf().optionalFieldOf("lore").forGetter(MenuItem::lore),
        MenuAction.CODEC.optionalFieldOf("action", new NoopAction()).forGetter(MenuItem::action)
    ).apply(instance, MenuItem::new));


    public ItemStack makeItemStack() {
        Item itemEntry = BuiltInRegistries.ITEM.getOptional(item)
            .orElse(net.minecraft.world.item.Items.BARRIER);

        if (action instanceof CraftItemsAction craftItemsAction) {
            return makeCraftingItemStack(craftItemsAction, itemEntry);
        }

        ItemStack stack = new ItemStack(itemEntry);
        if (!name.isEmpty()) stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        var lore = this.lore.orElse(List.of());
        if (!lore.isEmpty()) {
            var itemLore = ItemLore.EMPTY;
            for (String line : lore) {
                itemLore = itemLore.withLineAdded(Component.literal(line));
            }
            stack.set(DataComponents.LORE, itemLore);
        }
        return stack;
    }

    private ItemStack makeCraftingItemStack(CraftItemsAction craftItemsAction, Item item) {
        ItemStack stack = new ItemStack(item);
        if (!name.isEmpty()) stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));


        var itemLore = new ArrayList<Component>();
        itemLore.add(Component.literal("§lInputs:§r"));
        for (CraftItem craftItem : craftItemsAction.inputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.getValue(craftItem.item());
            itemLore.add(Component.literal(" - " + craftItem.quantity() + "x " + craftItemEntry.getName().getString()));
        }
        itemLore.add(Component.literal(""));

        itemLore.add(Component.literal(String.format("§lOutput%s:§r", craftItemsAction.outputs().size() == 1 ? "" : "s")));
        for (CraftItem craftItem : craftItemsAction.outputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.getValue(craftItem.item());
            itemLore.add(Component.literal(" - " + craftItem.quantity() + "x " + craftItemEntry.getName().getString()));
        }

        stack.set(DataComponents.LORE, new ItemLore(itemLore));

        return stack;
    }
}

