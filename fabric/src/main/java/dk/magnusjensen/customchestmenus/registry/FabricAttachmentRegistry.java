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

package dk.magnusjensen.customchestmenus.registry;

import dk.magnusjensen.customchestmenus.Constants;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.impl.attachment.AttachmentRegistryImpl;
import net.minecraft.resources.ResourceLocation;

public class FabricAttachmentRegistry {
    public static final AttachmentType<PlayerDataAttachment> PLAYER_DATA = AttachmentRegistry.create(
        ResourceLocation.tryBuild(Constants.MOD_ID, "player_data"),
        (builder) -> builder.initializer(() -> new PlayerDataAttachment())
            .syncWith(PlayerDataAttachment.SYNC_CODEC, (attachmentTarget, serverPlayer) -> {
                return true; // Sync all?
            })
    );

    public static <T> AttachmentType<T> findById(ResourceLocation id) {
        var attachmentType = AttachmentRegistryImpl.get(id);

        return (AttachmentType<T>) attachmentType;
    }

    public static void register() {
        Constants.LOGGER.info("Registering attachments for Fabric.");
    }
}
