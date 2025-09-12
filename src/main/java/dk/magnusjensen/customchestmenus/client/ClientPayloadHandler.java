package dk.magnusjensen.customchestmenus.client;

import dk.magnusjensen.customchestmenus.client.screen.CustomChestScreen;
import dk.magnusjensen.customchestmenus.network.UpdateMenuTitleS2C;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;

public class ClientPayloadHandler {
    public static void handleTitleUpdate(final UpdateMenuTitleS2C packet, final IPayloadContext context) {
        if (!(Minecraft.getInstance().screen instanceof CustomChestScreen ccs) || !Objects.equals(ccs.getMenu().menuId(), packet.menuId()))  {
            return; // Do nothing
        }

        ccs.setDynamicTitle(packet.title());
    }
}
