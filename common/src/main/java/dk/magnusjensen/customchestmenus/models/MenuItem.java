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

package dk.magnusjensen.customchestmenus.models;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction;
import dk.magnusjensen.customchestmenus.models.actions.MenuAction;
import dk.magnusjensen.customchestmenus.models.actions.NoopAction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.Map;

public class MenuItem extends BaseItem {

    public static final Codec<MenuItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BaseItem.MAP_CODEC.forGetter(mi -> mi),
        Codec.INT.fieldOf("slot").forGetter(MenuItem::slot),
        MenuAction.CODEC_UNIFIED.optionalFieldOf("action", new NoopAction()).forGetter(MenuItem::action)
    ).apply(instance, (baseItem, slot, menuAction) -> new MenuItem(
        baseItem.item(),
        baseItem.name(),
        baseItem.count(),
        baseItem.components(),
        slot,
        menuAction
    )));


    private final MenuAction action;
    private final int slot;


    public MenuItem(ResourceLocation item, String name, int count, Map<DataComponentType<?>, Object> components, int slot, MenuAction action) {
        super(item, name, count, components);
        this.slot = slot;
        this.action = action;
    }

    public MenuAction action() {
        return action;
    }

    @Override
    public ItemStack makeItemStack() {
        var stack =  super.makeItemStack();

        if (action instanceof CraftItemsAction craftItemsAction) {
            return makeCraftingItemStack(craftItemsAction, stack);
        }

        return stack;
    }

    private ItemStack makeCraftingItemStack(CraftItemsAction craftItemsAction, ItemStack stack) {
        if (craftItemsAction.hideText()) {
            return stack; // Don't add lore, and since we don't do anything else we exit early.
        }

        ItemLore itemLore = stack.get(DataComponents.LORE);
        if (itemLore == null) itemLore = ItemLore.EMPTY;

        itemLore = itemLore.withLineAdded(Component.literal("§lInputs:§r"));
        for (BaseItem craftItem : craftItemsAction.inputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.get(craftItem.item());
            itemLore = itemLore.withLineAdded(Component.literal(" - " + craftItem.count() + "x " + craftItemEntry.getDescription().getString()));
        }
        itemLore = itemLore.withLineAdded(Component.literal(""));

        itemLore = itemLore.withLineAdded(Component.literal(String.format("§lOutput%s:§r", craftItemsAction.outputs().size() == 1 ? "" : "s")));
        for (BaseItem craftItem : craftItemsAction.outputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.get(craftItem.item());
            itemLore = itemLore.withLineAdded(Component.literal(" - " + craftItem.count() + "x " + craftItemEntry.getDescription().getString()));
        }

        stack.set(DataComponents.LORE, itemLore);
        return stack;
    }


    public int slot() {
        return slot;
    }

}
