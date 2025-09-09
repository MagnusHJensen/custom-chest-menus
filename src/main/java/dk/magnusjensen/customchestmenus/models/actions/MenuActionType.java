package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

public enum MenuActionType {
    NOOP("noop", NoopAction.CODEC),
    CLOSE("close", CloseAction.CODEC),
    TELEPORT("teleport", TeleportAction.CODEC),
    NEXT_PAGE("next_page", PageAction.NEXT_CODEC),
    PREVIOUS_PAGE("previous_page", PageAction.PREVIOUS_CODEC);

    private final String id;
    private final MapCodec<? extends MenuAction> codec;

    MenuActionType(String id, MapCodec<? extends MenuAction> codec) {
        this.id = id;
        this.codec = codec;
    }

    public String id() {
        return id;
    }

    public static final Codec<MenuActionType> TYPE_CODEC = Codec.STRING.flatXmap(
        s -> byId(s).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown action type: " + s)),
        t -> DataResult.success(t.id)
    );

    public static java.util.Optional<MenuActionType> byId(String id) {
        for (var t : values()) if (t.id.equals(id)) return java.util.Optional.of(t);
        return java.util.Optional.empty();
    }

    // Used by dispatch(...) -> must return a MapCodec
    public MapCodec<? extends MenuAction> subCodec() {
        return this.codec;
    }
}