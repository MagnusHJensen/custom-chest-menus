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

import dk.magnusjensen.customchestmenus.commands.CommandHandler;
import dk.magnusjensen.customchestmenus.menu.CustomChestMenu;
import dk.magnusjensen.customchestmenus.models.MenuDefinition;
import dk.magnusjensen.customchestmenus.models.PagePayload;
import dk.magnusjensen.customchestmenus.network.PacketHandler;
import dk.magnusjensen.customchestmenus.registries.CustomChestMenuRegistry;
import dk.magnusjensen.customchestmenus.registries.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(CustomChestMenus.MODID)
@Mod.EventBusSubscriber(modid = CustomChestMenus.MODID)
public class CustomChestMenus {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "customchestmenus";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogManager.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CustomChestMenus() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        MenuRegistry.MENUS.register(modBus);
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        //modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        PacketHandler.register();
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        CustomChestMenuRegistry.loadMenus(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandHandler.register(event.getDispatcher());
    }

    public static void openMenu(ServerPlayer player, MenuDefinition def, int pageIndex) {
        PagePayload payload = def.build(def, pageIndex);

        MenuProvider provider = new MenuProvider() {
            @Override public Component getDisplayName() { return payload.title(); }

            @Override
            public CustomChestMenu createMenu(int windowId, Inventory inv, Player p) {
                // Server container with authoritative contents:
                CustomChestMenu cont = new CustomChestMenu(windowId, inv, def.id(), payload);
                cont.populateFromDefinition(def, pageIndex);  // Fill backing SimpleContainer on server
                return cont;
            }
        };

        NetworkHooks.openScreen(player, provider, buf -> {
            buf.writeUtf(def.id());
            payload.write(buf);
        });
    }

    public static void openMenu(ServerPlayer player, String menuId, int pageIndex) {
        MenuDefinition def = CustomChestMenuRegistry.get(menuId);
        if (def == null) return;

        openMenu(player, def, pageIndex);
    }
}
