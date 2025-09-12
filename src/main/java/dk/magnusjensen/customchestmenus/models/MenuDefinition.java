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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
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
            filler = f.makeItemStack();
        }

        List<PagePayload.Entry> entries = new ArrayList<>();
        for (MenuItem it : page.items()) {
            entries.add(new PagePayload.Entry(it.slot(), it.makeItemStack()));
        }

        return new PagePayload(size, Component.literal(page.title()), filler, entries);
    }
}
