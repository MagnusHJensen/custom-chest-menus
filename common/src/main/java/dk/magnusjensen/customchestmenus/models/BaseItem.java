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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;

public class BaseItem {

    public static final MapCodec<BaseItem> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("item").forGetter(BaseItem::item),
        ComponentSerialization.CODEC.optionalFieldOf("name", Component.empty()).forGetter(BaseItem::name),
        Codec.INT.optionalFieldOf("count", 1).forGetter(BaseItem::count),
        DataComponentType.VALUE_MAP_CODEC.optionalFieldOf("components", Map.of()).forGetter(BaseItem::components)
    ).apply(instance, BaseItem::new));

    public static final Codec<BaseItem> CODEC = MAP_CODEC.codec();

    private final ResourceLocation item;
    private final Component name;
    private final int count;
    private final Map<DataComponentType<?>, Object> components;

    public BaseItem(
        ResourceLocation item,
        Component name,
        int count,
        Map<DataComponentType<?>, Object> components
    ) {
        this.item = item;
        this.name = name;
        this.count = count;
        this.components = components;
    }




    public ItemStack makeItemStack() {
        Item itemEntry = BuiltInRegistries.ITEM.getOptional(item)
            .orElse(net.minecraft.world.item.Items.BARRIER);

        ItemStack stack = new ItemStack(itemEntry);
        stack.setCount(this.count);

        // For now, we allow overriding the custom name in the components, since that takes a proper Component codec.
        if (!Objects.equals(name, Component.empty()))
            stack.set(DataComponents.CUSTOM_NAME, name);


        for (var entry : components.entrySet()) {
            stack.set((DataComponentType) entry.getKey(), entry.getValue());
        }

        return stack;
    }

    public ResourceLocation item() {
        return item;
    }

    public Component name() {
        return name;
    }

    public int count() {
        return count;
    }

    public Map<DataComponentType<?>, Object> components() {
        return components;
    }
}
