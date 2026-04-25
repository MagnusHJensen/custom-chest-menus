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

package dk.magnusjensen.customchestmenus.platform;

import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.PagePayload;
import dk.magnusjensen.customchestmenus.platform.services.INetworkHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoforgeNetworkHelper implements INetworkHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public void openChestMenuScreen(ServerPlayer player, MenuDefinition definition, int pageIndex) {
        PagePayload payload = definition.build(pageIndex);

        MenuProvider provider = new MenuProvider() {
            @Override public Component getDisplayName() { return payload.title(); }

            @Override
            public CustomChestMenu createMenu(int windowId, Inventory inv, Player p) {
                // Server container with authoritative contents:
                CustomChestMenu cont = new CustomChestMenu(windowId, inv, new CustomChestMenu.Payload(definition.id(), payload));
                cont.populateFromDefinition(definition, pageIndex);  // Fill backing SimpleContainer on server
                return cont;
            }
        };

        player.openMenu(provider, buf -> {
            CustomChestMenu.Payload.STREAM_CODEC.encode(buf, new CustomChestMenu.Payload(definition.id(), payload));
        });
    }
}
