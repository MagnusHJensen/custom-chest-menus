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

import dk.magnusjensen.customchestmenus.events.EventHandler;
import dk.magnusjensen.customchestmenus.network.FabricNetwork;
import dk.magnusjensen.customchestmenus.registry.FabricAttachmentRegistry;
import dk.magnusjensen.customchestmenus.registry.FabricMenuRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.InteractionResult;

public class FabricCustomChestMenus implements ModInitializer {
    
    @Override
    public void onInitialize() {

        CommonClass.init();
        FabricNetwork.register();

        // Register commands
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> CommonClass.registerCommands(commandDispatcher)));

        ServerLifecycleEvents.SERVER_STARTING.register(CommonClass::loadMenus);

        // Register event handlers
        UseBlockCallback.EVENT.register((player, level, interactionHand, blockHitResult) -> {
            var handled = EventHandler.onBlockRightClick(player, blockHitResult.getBlockPos(), interactionHand);
            if (handled) {
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, interactionHand, entity, entityHitResult) -> {
            if (entityHitResult == null) {
                return InteractionResult.PASS;
            }

            var handled = EventHandler.onEntityRightClick(player, entity, interactionHand);
            if (handled) {
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, serverLevel) -> EventHandler.onEntityUnload(entity));
        EntityTrackingEvents.START_TRACKING.register((entity, serverPlayer) -> EventHandler.onPlayerStartTracking(serverPlayer, entity));
        EntityTrackingEvents.STOP_TRACKING.register((entity, serverPlayer) -> EventHandler.onPlayerStopTracking(serverPlayer, entity));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register(((serverPlayer, origin, destination) -> {
            EventHandler.onPlayerChangeDimension(serverPlayer, destination);
        }));
        ServerPlayerEvents.LEAVE.register(EventHandler::onPlayerLeaveServer);


        // Static load registries
        FabricMenuRegistry.register();
        FabricAttachmentRegistry.register();
    }

}
