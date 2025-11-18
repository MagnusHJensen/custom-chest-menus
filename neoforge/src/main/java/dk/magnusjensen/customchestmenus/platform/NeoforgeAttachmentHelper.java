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

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.platform.services.IAttachmentHelper;
import dk.magnusjensen.customchestmenus.registry.NeoforgeAttachmentRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Optional;

public class NeoforgeAttachmentHelper implements IAttachmentHelper {
    @Override
    public <T> T getPlayerAttachment(Player player, ResourceLocation id) {
        Optional<AttachmentType<T>> attachmentType = NeoforgeAttachmentRegistry.findById(id);
        return attachmentType.map(player::getData).orElse(null);
    }

    @Override
    public <T> void setPlayerAttachment(ServerPlayer player, T attachment, ResourceLocation id) {
        Optional<AttachmentType<T>> attachmentType = NeoforgeAttachmentRegistry.findById(id);

        if (attachmentType.isEmpty()) {
            Constants.LOGGER.error("No attachment found for {}", attachment.getClass());
            return;
        }

        player.setData(attachmentType.get(), attachment);
    }
}
