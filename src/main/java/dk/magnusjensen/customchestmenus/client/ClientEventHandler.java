package dk.magnusjensen.customchestmenus.client;

import dk.magnusjensen.customchestmenus.CustomChestMenus;
import dk.magnusjensen.customchestmenus.client.screen.CustomChestScreen;
import dk.magnusjensen.customchestmenus.registries.MenuRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = CustomChestMenus.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.CUSTOM_CHEST_MENU.get(), CustomChestScreen::new);
    }
}
