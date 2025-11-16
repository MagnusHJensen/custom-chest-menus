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
import dk.magnusjensen.customchestmenus.commands.arguments.MenuArgument;
import dk.magnusjensen.customchestmenus.data.ChestMenuSavedData;
import dk.magnusjensen.customchestmenus.data.PlayerDataAttachment;
import dk.magnusjensen.customchestmenus.platform.Services;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class BindingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(CommandHandler.COMMAND_ROOT)
            .then(Commands.literal("bind")
                .then(Commands.argument("menu", StringArgumentType.word())
                    .suggests(MenuArgument.MENU_IDS)
                    .executes(ctx -> {
                        var menuDef = MenuArgument.requireMenu(ctx, "menu");
                        var player = ctx.getSource().getPlayerOrException();

                        var playerData = Services.ATTACHMENT.getPlayerAttachment(player, PlayerDataAttachment.class);
                        if (playerData != null && !playerData.isBindingMode()) {
                            playerData.activateBindingMode(menuDef.id());
                            Services.ATTACHMENT.setPlayerAttachment(player, playerData);
                            player.sendSystemMessage(Component.literal("Binding mode activated for menu: " + menuDef.id() + ". Right-click a block/entity to bind it."), true);
                        } else if (playerData != null && playerData.isBindingMode()) {
                            playerData.disableBindingMode();
                            Services.ATTACHMENT.setPlayerAttachment(player, playerData);
                            player.sendSystemMessage(Component.literal("Binding mode disabled."), true);
                        }

                        return 1;
                    })
                )
            )
            .then(Commands.literal("unbind")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();

                    var playerData = Services.ATTACHMENT.getPlayerAttachment(player, PlayerDataAttachment.class);
                    if (playerData != null && !playerData.isUnbindingMode()) {
                        playerData.activateUnbindingMode();
                        Services.ATTACHMENT.setPlayerAttachment(player, playerData);
                        player.sendSystemMessage(Component.literal("Unbinding mode activated. Right-click a block/entity to unbind it."), true);
                    } else if (playerData != null && playerData.isUnbindingMode()) {
                        playerData.disableBindingMode();
                        Services.ATTACHMENT.setPlayerAttachment(player, playerData);
                        player.sendSystemMessage(Component.literal("Unbinding mode disabled."), true);
                    }

                    return 1;
                })
            )
            .then(Commands.literal("stop-binding") // Stops all bindings
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();

                    var playerData = Services.ATTACHMENT.getPlayerAttachment(player, PlayerDataAttachment.class);
                    if (playerData != null && (playerData.isBindingMode() || playerData.isUnbindingMode())) {
                        playerData.disableBindingMode();
                        Services.ATTACHMENT.setPlayerAttachment(player, playerData);
                        player.sendSystemMessage(Component.literal("Stopped binding/unbinding mode."), true);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("list-bindings")
                .executes(ctx -> {
                    var savedData = ctx.getSource().getLevel().getDataStorage().computeIfAbsent(ChestMenuSavedData.ID);

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
                                    .withClickEvent(new ClickEvent.RunCommand(
                                        String.format("/tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ())
                                    ))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal("Click to teleport to this block")
                                    ))
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
                                    .withClickEvent(new ClickEvent.RunCommand(
                                        String.format("/tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ())
                                    ))
                                    .withHoverEvent(new HoverEvent.ShowText(
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
        );
    }
}
