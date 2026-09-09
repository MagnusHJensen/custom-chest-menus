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

import dk.magnusjensen.customchestmenus.exception.CraftFailedException;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.models.BaseItem;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.MenuSize;
import dk.magnusjensen.customchestmenus.models.actions.CommandAction;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction;
import dk.magnusjensen.customchestmenus.models.actions.PageAction;
import dk.magnusjensen.customchestmenus.models.actions.TeleportAction;
import dk.magnusjensen.customchestmenus.network.UpdateMenuTitleS2C;
import dk.magnusjensen.customchestmenus.platform.Services;
import dk.magnusjensen.customchestmenus.registry.CustomChestMenuRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ActionExecutor {

    public static void onClick(ServerPlayer player, String menuId, int pageIndex, int slot, boolean isShiftDown) {
        var menu = CustomChestMenuRegistry.get(menuId);
        if (menu == null) return;
        if (!inBounds(menu.size(), slot)) return;

        var page = menu.pages().get(Math.max(0, Math.min(pageIndex, menu.pages().size()-1)));
        var item = page.items().stream().filter(it -> it.slot() == slot).findFirst().orElse(null);
        if (item == null) return;

        switch (item.action().type()) {
            case CLOSE -> player.closeContainer();
            case NEXT_PAGE -> openPage(player, menu, pageIndex, Math.min(pageIndex + 1, menu.pages().size()-1));
            case PREVIOUS_PAGE -> openPage(player, menu, pageIndex, Math.max(pageIndex - 1, 0));
            case JUMP_TO_PAGE -> {
                PageAction pageAction = (PageAction) item.action();
                openPage(player, menu, pageIndex, org.joml.Math.clamp(pageAction.targetPage().orElse(0), 0, menu.pages().size()-1));
            }
            case TELEPORT -> {
                TeleportAction tp = (TeleportAction) item.action();
                doTeleport(player, tp);
                // optional: close after teleport
                player.closeContainer();
            }
            case COMMAND -> {
                CommandAction command = (CommandAction) item.action();
                runCommand(player, command);
            }
            case CRAFT_ITEMS -> {
                CraftItemsAction action = (CraftItemsAction) item.action();
                craftItems(player, action, isShiftDown);
            }
        }

        if (item.shouldCloseOnClick()) {
            player.closeContainer();
        }
    }

    private static void openPage(ServerPlayer player, MenuDefinition menu, int currentPage, int newPage) {
        if (!(player.containerMenu instanceof CustomChestMenu ccm) || !ccm.menuId().equals(menu.id())) return;

        if (newPage == currentPage) return; // nothing to do

        // Fast path: just swap page content (title stays as-is)
        ccm.populateFromDefinition(menu, newPage);

        var clampedPage = org.joml.Math.clamp(0, menu.pages().size()-1, newPage);
        var packet = new UpdateMenuTitleS2C(ccm.menuId(), menu.pages().get(clampedPage).titleAsComponent());
        Services.NETWORK.sendToPlayer(player, packet);
    }

    private static void doTeleport(ServerPlayer player, TeleportAction tp) {
        ServerLevel target = player.getServer().getLevel(
            tp.dimension().map(ResourceLocation::tryParse)
                .map(rl -> ResourceKey.create(Registries.DIMENSION, rl))
                .orElse(player.level().dimension())
        );
        if (target == null) return;
        player.teleportTo(target, tp.x() + 0.5, tp.y(), tp.z() + 0.5, Set.of(), player.getYRot(), player.getXRot());
    }

    private static boolean inBounds(MenuSize size, int slot) {
        int max = size == MenuSize.SINGLE ? 27 : 54;
        return slot >= 0 && slot < max;
    }

    private static void runCommand(ServerPlayer player, CommandAction action) {
        MinecraftServer server = player.getServer();
        CommandSourceStack stack = server.createCommandSourceStack();
        if (action.shouldRunAsPlayer()) {
            stack = player.createCommandSourceStack();
        }

        for (String cmd : action.commands()) {
            String finalCmd = cmd
                .replace("%player%", player.getScoreboardName())
                .replace("%uuid%", player.getStringUUID());
            server.getCommands().performPrefixedCommand(stack, finalCmd);
        }
    }

    private static void craftItems(ServerPlayer player, CraftItemsAction action, boolean isShiftDown) {
        try {
            for (ItemStack leftover : craftItems(player.getInventory(), action, isShiftDown)) {
                // If inventory is full, drop the item in the world
                player.drop(leftover, false);
            }
        } catch (CraftFailedException e) {
            // Keeps the component intact, so translatable item names stay client side.
            player.sendSystemMessage(e.componentMessage());
        } catch (RuntimeException e) {
            player.sendSystemMessage(Component.literal(e.getMessage()));
        } catch(Exception e) {
            player.sendSystemMessage(Component.literal("An unexpected error occurred while crafting items."));
            Constants.LOGGER.error("Unexpected error while crafting items for player {}: {}", player.getScoreboardName(), e.getMessage(), e);
        }

    }

    /**
     * Consumes the inputs of the action from the inventory and hands out the outputs.
     * Throws a {@link CraftFailedException} if the inventory does not have enough of the required inputs.
     * @return the outputs that did not fit in the inventory, and therefore have to be dropped.
     */
    static List<ItemStack> craftItems(Inventory inventory, CraftItemsAction action, boolean craftAll) {
        var requirements = action.requirements();
        int maxCountOfCrafts = action.maxRepeats(inventory, requirements);
        if (maxCountOfCrafts < 1) {
            throw new CraftFailedException(action.firstShortfall(inventory, requirements)
                .map(action::missingMessageFor)
                .orElse(Component.literal("This item cannot be crafted.")));
        }

        int repeats = craftAll ? maxCountOfCrafts : 1;
        var slotToQuantity = action.plan(inventory, requirements, repeats).orElseThrow();

        // We can safely removeItems here, as the canCraft checks quantity and items being in the inventory.
        for (Map.Entry<Integer, Integer> entry : slotToQuantity.entrySet()) {
            // Remove the items from the inventory
            inventory.removeItem(entry.getKey(), entry.getValue());
        }

        // Add the output items
        List<ItemStack> leftovers = new ArrayList<>();
        for (BaseItem item : action.outputs()) {
            for (int i = 0; i < repeats; i++) {
                ItemStack toGive = item.makeItemStack();
                if (!inventory.add(toGive)) {
                    while (!toGive.isEmpty()) {
                        leftovers.add(toGive.split(toGive.getMaxStackSize()));
                    }
                }
            }

        }
        return leftovers;
    }
}

