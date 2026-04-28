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

package dk.magnusjensen.customchestmenus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dk.magnusjensen.customchestmenus.Memory;
import dk.magnusjensen.customchestmenus.Utils;
import dk.magnusjensen.customchestmenus.commands.arguments.MenuArgument;
import dk.magnusjensen.customchestmenus.data.ChestMenuSavedData;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.permissions.CCMPermission;
import dk.magnusjensen.customchestmenus.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.saveddata.SavedData;

public class BindingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(CommandHandler.COMMAND_ROOT)
            .then(Commands.literal("bind")
                .requires(Services.PERMISSION.getCommandPermissionPredicate(CCMPermission.BIND))
                .then(Commands.argument("menu", StringArgumentType.word())
                    .suggests(MenuArgument.MENU_IDS)
                    .executes(ctx -> {
                        var menuDef = MenuArgument.requireMenu(ctx, "menu");
                        var player = ctx.getSource().getPlayerOrException();

                        var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
                        if (playerData != null && !playerData.isBindingMode()) {
                            playerData.activateBindingMode(menuDef.id());
                            player.sendSystemMessage(Component.literal("Binding mode activated for menu: " + menuDef.id() + ". Right-click a block/entity to bind it."), true);
                        } else if (playerData != null && playerData.isBindingMode()) {
                            playerData.disableBindingMode();
                            player.sendSystemMessage(Component.literal("Binding mode disabled."), true);
                        }

                        var newPlayerData = new PlayerDataAttachment();
                        newPlayerData.copyFrom(playerData);
                        Services.ATTACHMENT.setPlayerAttachment(player, newPlayerData, newPlayerData.getId());

                        return 1;
                    })
                )
            )
            .then(Commands.literal("unbind")
                .requires(Services.PERMISSION.getCommandPermissionPredicate(CCMPermission.UNBIND))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();

                    var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
                    if (playerData != null && !playerData.isUnbindingMode()) {
                        playerData.activateUnbindingMode();
                        player.sendSystemMessage(Component.literal("Unbinding mode activated. Right-click a block/entity to unbind it."), true);
                    } else if (playerData != null && playerData.isUnbindingMode()) {
                        playerData.disableBindingMode();
                        player.sendSystemMessage(Component.literal("Unbinding mode disabled."), true);
                    }

                    var newPlayerData = new PlayerDataAttachment();
                    newPlayerData.copyFrom(playerData);
                    Services.ATTACHMENT.setPlayerAttachment(player, newPlayerData, newPlayerData.getId());


                    return 1;
                })
            )
            .then(Commands.literal("stop-binding") // Stops all bindings
                .requires(Services.PERMISSION.getCommandPermissionPredicate(CCMPermission.STOP_BIND))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();

                    var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
                    if (playerData != null && (playerData.isBindingMode() || playerData.isUnbindingMode())) {
                        playerData.disableBindingMode();
                        var newPlayerData = new PlayerDataAttachment();
                        newPlayerData.copyFrom(playerData);
                        Services.ATTACHMENT.setPlayerAttachment(player, newPlayerData, newPlayerData.getId());
                        player.sendSystemMessage(Component.literal("Stopped binding/unbinding mode."), true);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("list-bindings")
                .requires(Services.PERMISSION.getCommandPermissionPredicate(CCMPermission.LIST_BINDS))
                .executes(ctx -> {
                    var savedData = ctx.getSource().getLevel().getDataStorage().computeIfAbsent(new SavedData.Factory<>(ChestMenuSavedData::new, ChestMenuSavedData::load, null), ChestMenuSavedData.CHEST_MENU_KEY);

                    var interactiveBlocks = savedData.getMenuBlocks().values();
                    if (interactiveBlocks.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("No blocks are currently bound to menus.\n"), false);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.literal("Bound Blocks:"), false);
                        for (var block : interactiveBlocks) {
                            var pos = block.pos();
                            var clickablePos = Component.literal(String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()))
                                .withStyle(style -> style
                                    .withColor(0x00FF00) // Green color
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ())))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to teleport to this block")))
                                );

                            ctx.getSource().sendSuccess(
                                () -> Component.literal(String.format("- Menu ID: %s, Block Type: %s, Position: ", block.menuId(), block.blockType()))
                                    .append(clickablePos),
                                false
                            );
                        }
                    }

                    var interactiveEntities = savedData.getMenuEntities().values();
                    if (interactiveEntities.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("No entities are currently bound to menus.\n"), false);
                    } else {
                        ctx.getSource().sendSuccess(() -> Component.literal("Bound Entities:"), false);
                        for (var entity : interactiveEntities) {
                            var pos = entity.lastSeen();
                            var clickablePos = Component.literal(String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()))
                                .withStyle(style -> style
                                    .withColor(0x00FF00) // Green color
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        String.format("/tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ())
                                    ))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Component.literal("Click to teleport to this entity")
                                    ))
                                );

                            ctx.getSource().sendSuccess(
                                () -> Component.literal(String.format("- Menu ID: %s, Entity Type: %s, Position: ", entity.menuId(), entity.entityType()))
                                    .append(clickablePos),
                                false
                            );
                        }
                    }

                    return 1;
                })
            )
            .then(Commands.literal("bind-overlay")
                .requires(Services.PERMISSION.getCommandPermissionPredicate(CCMPermission.BIND_OVERLAY))
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();

                    var playerData = Services.ATTACHMENT.<PlayerDataAttachment>getPlayerAttachment(player, PlayerDataAttachment.ID);
                    var newPlayerData = new PlayerDataAttachment();
                    playerData.setHasOverlay(!playerData.hasOverlay()); // Toggle the overlay.

                    newPlayerData.copyFrom(playerData);

                    if (newPlayerData.hasOverlay()) {
                        // Set initial data.
                        Utils.populatePlayerDataForSync(ctx.getSource().getLevel(), newPlayerData, player);
                        Memory.playersWithMenuHighlightEnabled.add(player.getUUID());
                    } else {
                        Memory.playersWithMenuHighlightEnabled.remove(player.getUUID());
                    }
                    Services.ATTACHMENT.setPlayerAttachmentSync(player, newPlayerData, PlayerDataAttachment.ID);


                    player.sendSystemMessage(Component.literal("Menu binding overlay " + (newPlayerData.hasOverlay() ? "enabled" : "disabled") + "."), true);
                    return 0;
                })
            )
        );
    }


}
