package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record PageAction(MenuActionType type, Optional<Integer> targetPage) implements MenuAction {
    public static final MapCodec<PageAction> NEXT_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("target_page_index").forGetter(PageAction::targetPage)
    ).apply(i, idx -> new PageAction(MenuActionType.NEXT_PAGE, idx)));

    public static final MapCodec<PageAction> PREVIOUS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.optionalFieldOf("target_page_index").forGetter(PageAction::targetPage)
    ).apply(i, idx -> new PageAction(MenuActionType.PREVIOUS_PAGE, idx)));
}
