package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.MapCodec;

public record CloseAction() implements MenuAction {
    public static final MapCodec<CloseAction> CODEC = MapCodec.unit(CloseAction::new);

    @Override
    public MenuActionType type() {
        return MenuActionType.CLOSE;
    }
}
