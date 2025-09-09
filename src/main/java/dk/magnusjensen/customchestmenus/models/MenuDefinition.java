package dk.magnusjensen.customchestmenus.models;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Single menu definition type, the codec will handle encoding and decoding the different versions, by delegating to versioned definitions, and mapping it back to this shared type.
 */
public record MenuDefinition(String id,
                             String name,
                             MenuSize size,
                             Optional<MenuItem> filler,
                             List<MenuPage> pages)
{
    public static final Codec<MenuDefinition> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<MenuDefinition, T>> decode(DynamicOps<T> ops, T input) {
            Dynamic<T> dyn = new Dynamic<>(ops, input);

            var verRes = dyn.get("format_version").asNumber().result();
            if (verRes.isEmpty()) {
                return DataResult.error(() -> "Missing format_version");
            }

            int version = verRes.get().intValue();
            return switch (version) {
                case 1 -> MenuDefinitionV1.CODEC.decode(ops, input)
                    .map(pair -> pair.mapFirst(MenuDefinitionV1::toMenuDefinition));
                default -> DataResult.error(() -> "Unsupported format_version: " + version);
            };
        }

        @Override
        public <T> DataResult<T> encode(MenuDefinition value, DynamicOps<T> ops, T prefix) {
            // If you don't need to write menus back to JSON, error out:
            return DataResult.error(() -> "Encoding MenuDefinition is not supported");
            // If you do want encoding later, pick a version and delegate to that version's encoder.
            // return MenuDefinitionV1.from(value).encode(ops, prefix).map(pr -> pr.getSecond());
        }
    };

    public PagePayload build(MenuDefinition def, int pageIndex) {
        var page = def.pages().get(pageIndex);

        ItemStack filler = ItemStack.EMPTY;
        if (def.filler().isPresent()) {
            var f = def.filler().get();
            filler = makeStack(f.item(), f.name(), f.lore().orElse(List.of()));
        }

        List<PagePayload.Entry> entries = new ArrayList<>();
        for (MenuItem it : page.items()) {
            entries.add(new PagePayload.Entry(it.slot(), makeStack(
                it.item(), it.name(), it.lore().orElse(List.of())
            )));
        }

        return new PagePayload(size, Component.literal(page.title()), filler, entries);
    }

    private static ItemStack makeStack(ResourceLocation itemId, String name, List<String> lore) {
        Item item = BuiltInRegistries.ITEM.getOptional(itemId)
            .orElse(net.minecraft.world.item.Items.BARRIER);
        ItemStack stack = new ItemStack(item);
        if (!name.isEmpty()) stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        if (!lore.isEmpty()) {
            // Build a lore component list appropriate for your MC version
            // (for 1.21.x you can set DataComponents.LORE; for older, use display tag)
        }
        return stack;
    }
}
