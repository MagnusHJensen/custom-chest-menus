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

package dk.magnusjensen.customchestmenus;

import dk.magnusjensen.customchestmenus.data.ChestMenuSavedData;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public class Utils {
    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    /**
     * Handles populating player data for syncing to the client
     * @param level
     * @param playerData
     * @param player
     */
    public static void populatePlayerDataForSync(ServerLevel level, PlayerDataAttachment playerData, ServerPlayer player) {
        playerData.resetServerSideVersionData(); // To ensure sync happens for Fabric
        var savedData = level.getDataStorage().computeIfAbsent(ChestMenuSavedData.ID);
        playerData.setBoundBlocks(savedData.getMenuBlocks().keySet().stream().toList());

        var boundEntities = new ArrayList<Integer>();
        savedData.getMenuEntities().keySet().stream().forEach(
            (uuid) -> {
                var entity = player.level().getEntity(uuid);
                if (entity != null) {
                    boundEntities.add(entity.getId());
                }
            }
        );
        playerData.setBoundEntities(boundEntities);

    }
}
