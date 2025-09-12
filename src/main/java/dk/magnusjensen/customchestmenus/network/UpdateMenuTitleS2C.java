package dk.magnusjensen.customchestmenus.network;

import dk.magnusjensen.customchestmenus.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateMenuTitleS2C(String menuId, Component title) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateMenuTitleS2C> TYPE = new CustomPacketPayload.Type<>(Utils.modLoc("update_menu_title_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMenuTitleS2C> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        UpdateMenuTitleS2C::menuId,
        ComponentSerialization.STREAM_CODEC,
        UpdateMenuTitleS2C::title,
        UpdateMenuTitleS2C::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
