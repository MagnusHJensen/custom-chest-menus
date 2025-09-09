package dk.magnusjensen.customchestmenus;

import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.MenuSize;
import dk.magnusjensen.customchestmenus.models.actions.TeleportAction;
import dk.magnusjensen.customchestmenus.registries.CustomChestMenuRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public final class ActionExecutor {
    private static final boolean REOPEN_FOR_TITLE = true;

    public static void onClick(ServerPlayer player, String menuId, int pageIndex, int slot) {
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
            case TELEPORT -> {
                TeleportAction tp = (TeleportAction) item.action();
                doTeleport(player, tp);
                // optional: close after teleport
                player.closeContainer();
            }
        }
    }

    private static void openPage(ServerPlayer player, MenuDefinition menu, int currentPage, int newPage) {
        if (!(player.containerMenu instanceof CustomChestMenu ccm) || !ccm.menuId().equals(menu.id())) return;

        if (newPage == currentPage) return; // nothing to do

        if (REOPEN_FOR_TITLE) {
            // Re-open to also update the title bar (screens don't auto-refresh titles)
            CustomChestMenus.openMenu(player, menu, newPage);
        } else {
            // Fast path: just swap page content (title stays as-is)
            ccm.populateFromDefinition(menu, newPage);
        }
    }

    private static void doTeleport(ServerPlayer player, TeleportAction tp) {
        ServerLevel target = player.getServer().getLevel(
            tp.dimension().map(ResourceLocation::tryParse)
                .map(rl -> ResourceKey.create(Registries.DIMENSION, rl))
                .orElse(player.level().dimension())
        );
        if (target == null) return;
        player.teleportTo(target, tp.x(), tp.y(), tp.z(), Set.of(), player.getYRot(), player.getXRot(), false);
    }

    private static boolean inBounds(MenuSize size, int slot) {
        int max = size == MenuSize.SINGLE ? 27 : 54;
        return slot >= 0 && slot < max;
    }
}

