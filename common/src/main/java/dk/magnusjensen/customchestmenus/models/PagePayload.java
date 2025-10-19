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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record PagePayload(
    MenuSize size,                    // 27 or 54
    Component title,             // page title
    ItemStack filler,  // may be ItemStack.EMPTY
    List<Entry> entries          // explicit slot -> stack
) {
    public record Entry(int slot, ItemStack stack) {}

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(size);
        ComponentSerialization.STREAM_CODEC.encode(buf, title);

        buf.writeBoolean(filler != null && !filler.isEmpty());
        if (filler != null && !filler.isEmpty()) ItemStack.STREAM_CODEC.encode(buf, filler);
        buf.writeVarInt(entries.size());
        for (Entry e : entries) {
            buf.writeVarInt(e.slot());
            ItemStack.STREAM_CODEC.encode(buf, e.stack());
        }
    }

    public static PagePayload read(RegistryFriendlyByteBuf buf) {
        MenuSize size = buf.readEnum(MenuSize.class);
        Component title = ComponentSerialization.STREAM_CODEC.decode(buf);
        ItemStack filler = buf.readBoolean() ? ItemStack.STREAM_CODEC.decode(buf) : ItemStack.EMPTY;
        int n = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int slot = buf.readVarInt();
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            entries.add(new Entry(slot, stack));
        }
        return new PagePayload(size, title, filler, entries);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Entry> ENTRY_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        Entry::slot,
        ItemStack.STREAM_CODEC, // Read/Write ItemStack
        Entry::stack,
        Entry::new // Constructor
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PagePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8.map(
            s -> MenuSize.valueOf(s.toUpperCase()),
            MenuSize::name
        ), PagePayload::size,
        ComponentSerialization.STREAM_CODEC, PagePayload::title,
        ItemStack.STREAM_CODEC, PagePayload::filler,
        ENTRY_STREAM_CODEC.apply(ByteBufCodecs.list()), PagePayload::entries,
        PagePayload::new // Constructor
    );


}

