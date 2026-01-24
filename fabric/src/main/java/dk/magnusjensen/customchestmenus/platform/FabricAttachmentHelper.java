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

import dk.magnusjensen.customchestmenus.platform.services.IAttachmentHelper;
import dk.magnusjensen.customchestmenus.registry.FabricAttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FabricAttachmentHelper implements IAttachmentHelper {
    @Override
    public <T> T getPlayerAttachment(Player player, Identifier id) {
        var attachmentType = FabricAttachmentRegistry.<T>findById(id);
        return player.getAttachedOrCreate(attachmentType);
    }

    @Override
    public <T> void setPlayerAttachment(ServerPlayer player, T attachment, Identifier id) {
        AttachmentType<T> attachmentType = FabricAttachmentRegistry.findById(id);

        player.setAttached(attachmentType, attachment);
    }
}
