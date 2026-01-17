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

package dk.magnusjensen.customchestmenus.events;

import dk.magnusjensen.customchestmenus.Memory;
import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.data.ChestMenuSavedData;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.models.interactivity.InteractiveBlock;
import dk.magnusjensen.customchestmenus.models.interactivity.InteractiveEntity;
import dk.magnusjensen.customchestmenus.platform.Services;
import dk.magnusjensen.customchestmenus.registry.CustomChestMenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

public class EventHandler {

    // ---- Block right click
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

        if (handleBlockBinding(pos, block, serverPlayer)) {
            return true;
        }

        if (checkMenuBlock(pos, serverPlayer)) {
            return true;
        }

        return false;
    }

    private static boolean handleBlockBinding(BlockPos pos, Block block, ServerPlayer serverPlayer) {
        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(serverPlayer, PlayerDataAttachment.ID);
        if (playerData == null) {
            return false;
        }

        if (!playerData.isBindingMode() && !playerData.isUnbindingMode()) {
            return false;
        }

        var savedData = ((ServerLevel)serverPlayer.level()).getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
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

                // Set player data attachments to sync
                var newPlayerData = new PlayerDataAttachment();
                newPlayerData.copyFrom(playerData);
                var boundBlocks = playerData.boundBlocks();
                boundBlocks.remove(pos);
                newPlayerData.setBoundBlocks(boundBlocks);
                Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, playerData.getId());
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

            // Set player data attachments to sync
            var newPlayerData = new PlayerDataAttachment();
            newPlayerData.copyFrom(playerData);
            newPlayerData.setBoundBlocks(savedData.getMenuBlocks().keySet().stream().toList());
            Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, playerData.getId());
            return true;
        }

        return false;
    }

    private static boolean checkMenuBlock(BlockPos pos, ServerPlayer player) {
        var savedData = ((ServerLevel)player.level()).getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        if (!savedData.getMenuBlocks().containsKey(pos)) {
            return false;
        }

        InteractiveBlock interactiveBlock = savedData.getMenuBlocks().get(pos);

        Services.NETWORK.openChestMenuScreen(player, CustomChestMenuRegistry.get(interactiveBlock.menuId()), 0);
        return true;
    }

    // ---- Entity right click
    public static boolean onEntityRightClick(Player player, Entity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return false;
        }

        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }


        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (handleEntityBinding(target, serverPlayer)) {
            return true;
        }

        if (checkMenuEntity(target, serverPlayer)) {
            return true;
        }

        return false;
    }

    private static boolean handleEntityBinding(Entity entity, ServerPlayer serverPlayer) {
        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(serverPlayer, PlayerDataAttachment.ID);
        if (playerData == null) {
            return false;
        }

        if (!playerData.isBindingMode() && !playerData.isUnbindingMode()) {
            return false;
        }

        var savedData = ((ServerLevel)serverPlayer.level()).getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        if (savedData.getMenuEntities().containsKey(entity.getUUID())) { // Entity is already bound

            if (playerData.isBindingMode()) {
                // and we try to bind a menu
                serverPlayer.sendSystemMessage(Component.literal("This entity is already bound to a menu, to unbind it run /ccm unbind and right click"), true);
                return true; // we want to "handle" the event, and cancel block interaction.
            }

            if (playerData.isUnbindingMode()) {
                // and we try to unbind
                savedData.removeMenuEntity(entity.getUUID());
                serverPlayer.sendSystemMessage(Component.literal("Successfully unbound the menu from this entity."), true);

                var boundEntities = playerData.boundEntities();
                boundEntities.remove(Integer.valueOf(entity.getId()));

                var newPlayerData = new PlayerDataAttachment();
                newPlayerData.copyFrom(playerData);
                newPlayerData.setBoundEntities(boundEntities);
                Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, playerData.getId());
                return true;
            }

            return false;
        }

        // Entity is not bound
        if (playerData.isUnbindingMode()) {
            // but trying to unbind
            serverPlayer.sendSystemMessage(Component.literal("This entity is not bound to any menu."), true);
            return true;
        } else if (playerData.isBindingMode()) {
            ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            savedData.addMenuEntity(new InteractiveEntity(playerData.getMenuToBind(), entity.getUUID(), entity.blockPosition(), entityType));
            serverPlayer.sendSystemMessage(Component.literal("Successfully bound menu '" + playerData.getMenuToBind() + "' to this entity."), true);

            // Set player data attachments to sync
            var boundEntities = playerData.boundEntities();
            boundEntities.add(entity.getId());

            var newPlayerData = new PlayerDataAttachment();
            newPlayerData.copyFrom(playerData);
            newPlayerData.setBoundEntities(boundEntities);
            Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, playerData.getId());
            return true;
        }

        return false;
    }

    private static boolean checkMenuEntity(Entity entity, ServerPlayer player) {
        var savedData = ((ServerLevel) player.level()).getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        var interactiveEntity = savedData.getMenuEntities().get(entity.getUUID());
        if (interactiveEntity == null) {
            return false;
        }

        Services.NETWORK.openChestMenuScreen(player, CustomChestMenuRegistry.get(interactiveEntity.menuId()), 0);
        return true;
    }


    // ---- Entity unload
    // 1. Keep lastSeenPos up to date, we only store the change on unload to avoid too many saves.

    public static void onEntityUnload(Entity entity) {
        if (entity.level().isClientSide() || !(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var savedData = serverLevel.getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        var interactiveEntity = savedData.getMenuEntities().get(entity.getUUID());
        if (interactiveEntity == null) {
            return;
        }

        savedData.addMenuEntity(interactiveEntity.setLastSeen(entity.blockPosition()));
    }


    // ---- Player stop tracking entity
    // 1. Re-sync player attachment if it's a bound entity and player has overlay on
    public static void onPlayerStopTracking(Player player, Entity entity) {
        if (player.level().isClientSide() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!Memory.playersWithMenuHighlightEnabled.contains(player.getUUID())) {
            return;
        }

        var savedData = serverLevel.getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        var interactiveEntity = savedData.getMenuEntities().get(entity.getUUID());
        if (interactiveEntity == null) {
            return;
        }

        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
        var boundEntities = playerData.boundEntities();
        var newPlayerData = new PlayerDataAttachment();
        newPlayerData.copyFrom(playerData);
        boundEntities.remove(Integer.valueOf(entity.getId()));
        newPlayerData.setBoundEntities(boundEntities);
        Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, PlayerDataAttachment.ID);
    }

    // ---- Player start tracking entity
    // 1. Rsync player attachment if it's a bound entity.
    public static void onPlayerStartTracking(Player player, Entity entity) {
        if (player.level().isClientSide() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!Memory.playersWithMenuHighlightEnabled.contains(player.getUUID())) {
            return;
        }

        var savedData = serverLevel.getDataStorage().computeIfAbsent(ChestMenuSavedData::load, ChestMenuSavedData::new, ChestMenuSavedData.CHEST_MENU_KEY);
        var interactiveEntity = savedData.getMenuEntities().get(entity.getUUID());
        if (interactiveEntity == null) {
            return;
        }

        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
        var boundEntities = playerData.boundEntities();
        boundEntities.add(entity.getId());

        var newPlayerData = new PlayerDataAttachment();
        newPlayerData.copyFrom(playerData);
        newPlayerData.setBoundEntities(boundEntities);
        Services.ATTACHMENT.setPlayerAttachmentSync(serverPlayer, newPlayerData, PlayerDataAttachment.ID);
    }


    // ---- Player change dimension
    // 1. Repopulate all sync data
    public static void onPlayerChangeDimension(ServerPlayer player, ServerLevel level) {
        if (!Memory.playersWithMenuHighlightEnabled.contains(player.getUUID())) {
            return;
        }

        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
        var newPlayerData = new PlayerDataAttachment();
        newPlayerData.copyFrom(playerData);
        Utils.populatePlayerDataForSync(level, newPlayerData, player);
        Services.ATTACHMENT.setPlayerAttachmentSync(player, newPlayerData, newPlayerData.getId());
    }

    // ---- On player leave
    // 1. Remove player UUID from memory map
    public static void onPlayerLeaveServer(ServerPlayer player) {
        Memory.playersWithMenuHighlightEnabled.remove(player.getUUID());
    }
}
