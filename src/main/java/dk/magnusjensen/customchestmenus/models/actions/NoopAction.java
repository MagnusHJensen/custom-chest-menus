package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.MapCodec;

public record NoopAction() implements MenuAction {
    public static final MapCodec<NoopAction> CODEC = MapCodec.unit(NoopAction::new);

    @Override
    public MenuActionType type() {
        return MenuActionType.NOOP;
    }
}
