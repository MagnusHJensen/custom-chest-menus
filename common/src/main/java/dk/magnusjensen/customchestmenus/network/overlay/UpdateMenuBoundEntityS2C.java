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

package dk.magnusjensen.customchestmenus.network.overlay;

import dk.magnusjensen.customchestmenus.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

// Handles keeping the entity ID list on the client in sync.
public record UpdateMenuBoundEntityS2C(int entityId, OverlayOperation operation) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateMenuBoundEntityS2C> TYPE = new CustomPacketPayload.Type<>(Utils.modLoc("add_menu_bound_entity_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMenuBoundEntityS2C> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, UpdateMenuBoundEntityS2C::entityId,
        StreamCodec.of(
            (buf, operation) -> buf.writeVarInt(operation.ordinal()), // Encode enum as ordinal
            buf -> OverlayOperation.values()[buf.readVarInt()]        // Decode ordinal back to enum
        ), UpdateMenuBoundEntityS2C::operation,
        UpdateMenuBoundEntityS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum OverlayOperation {
        ADD,
        REMOVE;
    }
}
