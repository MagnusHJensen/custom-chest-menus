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

import dk.magnusjensen.customchestmenus.models.v2.MenuSize;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PagePayload(
    MenuSize size,
    Component title,             // page title
    MenuBackground background,
    ItemStack filler,  // may be ItemStack.EMPTY
    List<Entry> entries          // explicit slot -> stack
) {
    public record Entry(int slot, ItemStack stack) {}

    public static final StreamCodec<RegistryFriendlyByteBuf, Entry> ENTRY_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        Entry::slot,
        ItemStack.STREAM_CODEC, // Read/Write ItemStack
        Entry::stack,
        Entry::new // Constructor
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PagePayload> STREAM_CODEC = StreamCodec.composite(
        MenuSize.STREAM_CODEC, PagePayload::size,
        ComponentSerialization.STREAM_CODEC, PagePayload::title,
        MenuBackground.STREAM_CODEC, PagePayload::background,
        ItemStack.OPTIONAL_STREAM_CODEC, PagePayload::filler,
        ENTRY_STREAM_CODEC.apply(ByteBufCodecs.list()), PagePayload::entries,
        PagePayload::new // Constructor
    );


}

