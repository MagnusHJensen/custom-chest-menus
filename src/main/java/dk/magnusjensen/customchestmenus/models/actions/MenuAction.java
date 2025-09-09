package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;

public sealed interface MenuAction permits NoopAction, CloseAction, TeleportAction, PageAction {
    MenuActionType type();

    Codec<MenuAction> CODEC = MenuActionType.TYPE_CODEC
        .dispatch("type", MenuAction::type, MenuActionType::subCodec);
}
