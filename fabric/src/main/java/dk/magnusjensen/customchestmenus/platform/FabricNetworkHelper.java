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
import dk.magnusjensen.customchestmenus.network.CustomPacket;
import dk.magnusjensen.customchestmenus.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacket packet) {
        var buffer = PacketByteBufs.create();
        packet.write(buffer);
        ServerPlayNetworking.send(player, packet.getId(), buffer);
    }

    public void openChestMenuScreen(ServerPlayer player, MenuDefinition definition, int pageIndex) {
        PagePayload payload = definition.build(pageIndex);
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer serverPlayer, FriendlyByteBuf friendlyByteBuf) {
                friendlyByteBuf.writeUtf(definition.id());
                payload.write(friendlyByteBuf);
            }

            @Override
            public Component getDisplayName() {
                return payload.title();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                CustomChestMenu menu = new CustomChestMenu(i, inventory, definition.id(), payload);
                menu.populateFromDefinition(definition, pageIndex);
                return menu;
            }
        });
    }
}
