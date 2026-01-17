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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Represents an item definition with a custom menu.
 */
public record MenuItem(
    int slot,
    ResourceLocation item,
    String name,
    int count,
    Optional<List<String>> lore,
    MenuAction action,
    Optional<CompoundTag> nbt
) {
    public static final Codec<MenuItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("slot").forGetter(MenuItem::slot),
        ResourceLocation.CODEC.fieldOf("item").forGetter(MenuItem::item),
        Codec.STRING.fieldOf("name").forGetter(MenuItem::name),
        Codec.INT.optionalFieldOf("count", 1).forGetter(MenuItem::count),
        Codec.STRING.listOf().optionalFieldOf("lore").forGetter(MenuItem::lore),
        MenuAction.CODEC.optionalFieldOf("action", new NoopAction()).forGetter(MenuItem::action),
        CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(MenuItem::nbt)
    ).apply(instance, MenuItem::new));


    public ItemStack makeItemStack() {
        Item itemEntry = BuiltInRegistries.ITEM.getOptional(item)
            .orElse(net.minecraft.world.item.Items.BARRIER);

        ItemStack stack = new ItemStack(itemEntry);
        if (!name.isEmpty()) stack.setHoverName(Component.literal(name));
        nbt.ifPresent(stack::setTag);
        stack.setCount(this.count);

        if (action instanceof CraftItemsAction craftItemsAction) {
            return makeCraftingItemStack(craftItemsAction, stack);
        }


        var lore = this.lore.orElse(List.of());
        if (!lore.isEmpty()) {
            ListTag loreTag = new ListTag();
            for (String line : lore) {

                loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line))));
            }
            CompoundTag tag = stack.getTag();
            CompoundTag display = tag.getCompound("display");
            display.put("Lore", loreTag);
            stack.setTag(tag);
        }
        return stack;
    }

    private ItemStack makeCraftingItemStack(CraftItemsAction craftItemsAction, ItemStack stack) {
        var loreTag = new ListTag();
        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("§lInputs:§r"))));
        for (CraftItem craftItem : craftItemsAction.inputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.get(craftItem.item());
            loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(" - " + craftItem.quantity() + "x " + craftItemEntry.getDescription().getString()))));
        }
        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(""))));

        loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(String.format("§lOutput%s:§r", craftItemsAction.outputs().size() == 1 ? "" : "s")))));
        for (CraftItem craftItem : craftItemsAction.outputs()) {
            Item craftItemEntry = BuiltInRegistries.ITEM.get(craftItem.item());
            loreTag.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(" - " + craftItem.quantity() + "x " + craftItemEntry.getDescription().getString()))));
        }

        CompoundTag tag = stack.getTag();
        CompoundTag display = tag.getCompound("display");
        display.put("Lore", loreTag);
        stack.setTag(tag);

        return stack;
    }
}
