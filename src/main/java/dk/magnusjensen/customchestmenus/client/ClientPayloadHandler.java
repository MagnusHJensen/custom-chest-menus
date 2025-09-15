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

package dk.magnusjensen.customchestmenus.client;

import dk.magnusjensen.customchestmenus.client.screen.CustomChestScreen;
import dk.magnusjensen.customchestmenus.network.UpdateMenuTitleS2C;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientPayloadHandler {
    public static void handleTitleUpdate(final UpdateMenuTitleS2C packet, Supplier<NetworkEvent.Context> context) {
        if (!(Minecraft.getInstance().screen instanceof CustomChestScreen ccs) || !Objects.equals(ccs.getMenu().menuId(), packet.menuId()))  {
            return; // Do nothing
        }

        ccs.setDynamicTitle(packet.title());
        context.get().setPacketHandled(true);
    }
}
