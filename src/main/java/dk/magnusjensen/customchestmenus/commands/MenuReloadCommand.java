package dk.magnusjensen.customchestmenus.commands;

import com.mojang.brigadier.CommandDispatcher;
import dk.magnusjensen.customchestmenus.registries.CustomChestMenuRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public class MenuReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ccm")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    MinecraftServer server = ctx.getSource().getServer();
                    CustomChestMenuRegistry.loadMenus(server);
                    // TODO: Reload open menus on all players.
                    return 1;
                })
            )
        );
    }
}
