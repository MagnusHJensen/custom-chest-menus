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

package dk.magnusjensen.customchestmenus.events;

import dk.magnusjensen.customchestmenus.data.ChestMenuSavedData;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.models.interactivity.InteractiveBlock;
import dk.magnusjensen.customchestmenus.platform.Services;
import dk.magnusjensen.customchestmenus.registry.CustomChestMenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class EventHandler {
    /**
     * @return A boolean, true if event should be cancelled
     */
    public static boolean onBlockRightClick(Player player, BlockPos pos, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return false;
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }


        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        Block block = serverPlayer.level().getBlockState(pos).getBlock();

        if (handleBinding(pos, block, serverPlayer)) {
            return true;
        }

        if (checkMenuBlock(pos, serverPlayer)) {
            return true;
        }

        return false;
    }

    private static boolean handleBinding(BlockPos pos, Block block, ServerPlayer serverPlayer) {
        var playerData = Services.ATTACHMENT.getPlayerAttachment(serverPlayer, PlayerDataAttachment.class);
        if (playerData == null) {
            return false;
        }

        if (!playerData.isBindingMode() && !playerData.isUnbindingMode()) {
            return false;
        }

        var savedData = serverPlayer.level().getDataStorage().computeIfAbsent(ChestMenuSavedData.ID);
        if (savedData.getMenuBlocks().containsKey(pos)) { // Block is already bound

            if (playerData.isBindingMode()) {
                // and we try to bind a menu
                serverPlayer.sendSystemMessage(Component.literal("This block is already bound to a menu, to unbind it run /ccm unbind"), true);
                return true; // we want to "handle" the event, and cancel block interaction.
            }

            if (playerData.isUnbindingMode()) {
                // and we try to unbind
                savedData.removeMenuBlock(pos);
                serverPlayer.sendSystemMessage(Component.literal("Successfully unbound the menu from this block."), true);
                return true;
            }

            return false;
        }

        // Block is not bound
        if (playerData.isUnbindingMode()) {
            // but trying to unbind
            serverPlayer.sendSystemMessage(Component.literal("This block is not bound to any menu."), true);
            return true;
        } else if (playerData.isBindingMode()) {
            ResourceLocation blockType = BuiltInRegistries.BLOCK.getKey(block);
            savedData.addMenuBlock(new InteractiveBlock(playerData.getMenuToBind(), pos, blockType));
            serverPlayer.sendSystemMessage(Component.literal("Successfully bound menu '" + playerData.getMenuToBind() + "' to this block."), true);
            return true;
        }

        return false;
    }

    private static boolean checkMenuBlock(BlockPos pos, ServerPlayer player) {
        var savedData = player.level().getDataStorage().computeIfAbsent(ChestMenuSavedData.ID);
        if (!savedData.getMenuBlocks().containsKey(pos)) {
            return false;
        }

        InteractiveBlock interactiveBlock = savedData.getMenuBlocks().get(pos);

        Services.NETWORK.openChestMenuScreen(player, CustomChestMenuRegistry.get(interactiveBlock.menuId()), 0);
        return true;
    }
}
