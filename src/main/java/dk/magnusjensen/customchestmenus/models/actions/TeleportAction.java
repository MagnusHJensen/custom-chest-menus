package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record TeleportAction(
    double x,
    double y,
    double z,
    Optional<String> dimension
) implements MenuAction {

    public static final MapCodec<TeleportAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.DOUBLE.fieldOf("x").forGetter(TeleportAction::x),
        Codec.DOUBLE.fieldOf("y").forGetter(TeleportAction::y),
        Codec.DOUBLE.fieldOf("z").forGetter(TeleportAction::z),
        Codec.STRING.optionalFieldOf("dimension").forGetter(TeleportAction::dimension)
    ).apply(i, TeleportAction::new));

    @Override
    public MenuActionType type() {
        return MenuActionType.TELEPORT;
    }
}
