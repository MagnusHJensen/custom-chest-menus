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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record UpdateMenuTitleS2C(String menuId, Component title) implements CustomPacket {

    public static final ResourceLocation ID = Utils.modLoc("update_menu_title");

    public UpdateMenuTitleS2C(final FriendlyByteBuf buf) {
        this(
            buf.readUtf(Short.MAX_VALUE),
            buf.readComponent()
        );
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(menuId);
        buffer.writeComponent(title);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}
