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

package dk.magnusjensen.customchestmenus.network;

import dk.magnusjensen.customchestmenus.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UpdateMenuTitleS2C(String menuId, Component title) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateMenuTitleS2C> TYPE = new CustomPacketPayload.Type<>(Utils.modLoc("update_menu_title_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMenuTitleS2C> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        UpdateMenuTitleS2C::menuId,
        ComponentSerialization.STREAM_CODEC,
        UpdateMenuTitleS2C::title,
        UpdateMenuTitleS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
