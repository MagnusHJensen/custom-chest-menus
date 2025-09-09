package dk.magnusjensen.customchestmenus.models;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record PagePayload(
    MenuSize size,                    // 27 or 54
    Component title,             // page title
    @Nullable ItemStack filler,  // may be ItemStack.EMPTY
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
}
