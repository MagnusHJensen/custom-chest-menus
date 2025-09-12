package dk.magnusjensen.customchestmenus.network;

import dk.magnusjensen.customchestmenus.CustomChestMenus;
import dk.magnusjensen.customchestmenus.client.ClientPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CustomChestMenus.MODID)
public class PacketHandler {

    @SubscribeEvent
    public static void registerNetworkPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            UpdateMenuTitleS2C.TYPE,
            UpdateMenuTitleS2C.STREAM_CODEC,
            ClientPayloadHandler::handleTitleUpdate
        );
    }
}
