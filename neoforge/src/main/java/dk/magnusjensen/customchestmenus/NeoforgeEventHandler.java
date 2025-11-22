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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoforgeEventHandler {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        var shouldCancel = EventHandler.onBlockRightClick(event.getEntity(), event.getPos(), event.getHand());
        event.setCanceled(shouldCancel);
    }

    @SubscribeEvent
    public static void onEntityRightClock(PlayerInteractEvent.EntityInteract event) {
        boolean shouldCancel = EventHandler.onEntityRightClick(event.getEntity(), event.getTarget(), event.getHand());
        event.setCanceled(shouldCancel);
    }

    @SubscribeEvent
    public static void onEntityUnload(EntityLeaveLevelEvent event) {
        EventHandler.onEntityUnload(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerStopTrackingEntity(PlayerEvent.StopTracking event) {
        EventHandler.onPlayerStopTracking(event.getEntity(), event.getTarget());
    }

    @SubscribeEvent
    public static void onPlayerStartTrackingEntity(PlayerEvent.StartTracking event) {
        EventHandler.onPlayerStartTracking(event.getEntity(), event.getTarget());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var destination = serverPlayer.level().getServer().getLevel(event.getTo());

        EventHandler.onPlayerChangeDimension(serverPlayer, destination);
    }

    @SubscribeEvent
    public static void onPlayerLeaverServer(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        EventHandler.onPlayerLeaveServer(serverPlayer);
    }
}
