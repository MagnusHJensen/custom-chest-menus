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

import dk.magnusjensen.customchestmenus.data.PlayerCapability;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.network.ForgeNetwork;
import dk.magnusjensen.customchestmenus.network.SyncAttachmentDataS2C;
import dk.magnusjensen.customchestmenus.platform.services.IAttachmentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class ForgeAttachmentHelper implements IAttachmentHelper {
    @Override
    public <T> T getPlayerAttachment(Player player, ResourceLocation id) {
        var cap = player.getCapability(PlayerCapability.PLAYER_CAPABILITY);
        var resolvedCap = cap.resolve();
        return (T) resolvedCap.orElse(null);
    }

    @Override
    public <T> void setPlayerAttachment(ServerPlayer player, T attachment, ResourceLocation id) {
        // NOOP, as capabilities is instance specific to a player, and we don't need to set it again.
    }

    @Override
    public <T> void setPlayerAttachmentSync(ServerPlayer player, T attachment, ResourceLocation id) {
        this.setPlayerAttachment(player, attachment, id);

        if (attachment instanceof PlayerDataAttachment playerDataAttachment) {
            ForgeNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new SyncAttachmentDataS2C(playerDataAttachment));
        }
    }
}

