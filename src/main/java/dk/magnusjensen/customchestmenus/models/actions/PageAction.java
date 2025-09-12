package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

    // jump_to_page: REQUIRED target_page_index >= 0
    private static final Codec<Integer> NON_NEG_INT = Codec.INT.flatXmap(
        n -> n >= 0 ? DataResult.success(n) : DataResult.error(() -> "target_page_index must be >= 0"),
        DataResult::success
    );

    public static final MapCodec<PageAction> JUMP_TO_PAGE_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        NON_NEG_INT.fieldOf("target_page_index")
            .forGetter(pa -> pa.targetPage()
                .orElseThrow(() -> new IllegalStateException("Missing target_page_index for jump_to_page")))
    ).apply(i, idx -> new PageAction(MenuActionType.JUMP_TO_PAGE, Optional.of(idx))));
}
