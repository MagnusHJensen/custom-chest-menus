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

package dk.magnusjensen.customchestmenus.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IAttachmentHelper {
    <T> T getPlayerAttachment(Player player, ResourceLocation id);
    <T> void setPlayerAttachment(ServerPlayer player, T attachment, ResourceLocation id);

    // Function that is primarily used for NeoForge to sync data attachments if a client field has changed.
    // Fabric just calls the other setPlayerAttachment
    <T> void setPlayerAttachmentSync(ServerPlayer player, T attachment, ResourceLocation id);
}
