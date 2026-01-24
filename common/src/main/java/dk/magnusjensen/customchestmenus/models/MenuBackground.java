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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.joml.Vector2i;

import java.util.Optional;

public record MenuBackground(Identifier texture, Vector2i size, boolean showPlayerInventory, Optional<Vector2i> titleLocation) {
    public static final MenuBackground DEFAULT = new MenuBackground(Identifier.tryParse("textures/gui/container/generic_54.png"), new Vector2i(0, 0), true, Optional.empty());

    public boolean isDefault() {
        return this.equals(DEFAULT);
    }

    private static final Codec<Vector2i> SIZE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("width").forGetter(Vector2i::x),
        Codec.INT.fieldOf("height").forGetter(Vector2i::y)
    ).apply(instance, Vector2i::new));

    private static final Codec<Vector2i> LOCATION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("x").forGetter(Vector2i::x),
        Codec.INT.fieldOf("y").forGetter(Vector2i::y)
    ).apply(instance, Vector2i::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Vector2i> SIZE_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, Vector2i::x,
        ByteBufCodecs.INT, Vector2i::y,
        Vector2i::new
    );

    public static final Codec<MenuBackground> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("texture").forGetter(MenuBackground::texture),
        SIZE_CODEC.fieldOf("size").forGetter(MenuBackground::size),
        Codec.BOOL.optionalFieldOf("show_player_inventory", true).forGetter(MenuBackground::showPlayerInventory),
        LOCATION_CODEC.optionalFieldOf("title_location").forGetter(v -> v.titleLocation)
    ).apply(instance, MenuBackground::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MenuBackground> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, MenuBackground::texture,
        SIZE_STREAM_CODEC, MenuBackground::size,
        ByteBufCodecs.BOOL, MenuBackground::showPlayerInventory,
        ByteBufCodecs.optional(SIZE_STREAM_CODEC), MenuBackground::titleLocation,
        MenuBackground::new
    );
}
