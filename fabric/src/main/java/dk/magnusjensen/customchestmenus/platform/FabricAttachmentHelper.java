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

package dk.magnusjensen.customchestmenus.platform;

import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.network.SyncAttachmentDataS2C;
import dk.magnusjensen.customchestmenus.platform.services.IAttachmentHelper;
import dk.magnusjensen.customchestmenus.registry.FabricAttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FabricAttachmentHelper implements IAttachmentHelper {
    @Override
    public <T> T getPlayerAttachment(Player player, ResourceLocation id) {
        var attachmentType = FabricAttachmentRegistry.<T>findById(id);
        return player.getAttachedOrCreate(attachmentType);
    }

    @Override
    public <T> void setPlayerAttachment(ServerPlayer player, T attachment, ResourceLocation id) {
        AttachmentType<T> attachmentType = FabricAttachmentRegistry.findById(id);

        player.setAttached(attachmentType, attachment);
    }

    @Override
    public <T> void setPlayerAttachmentSync(ServerPlayer player, T attachment, ResourceLocation id) {
        this.setPlayerAttachment(player, attachment, id);
        if (attachment instanceof PlayerDataAttachment playerDataAttachment) {
            var buffer = PacketByteBufs.create();
            var packet = new SyncAttachmentDataS2C(playerDataAttachment);
            packet.write(buffer);
            ServerPlayNetworking.send(player, packet.getId(), buffer);
        }
    }
}
