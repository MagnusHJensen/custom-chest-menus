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

package dk.magnusjensen.customchestmenus.models.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record PageAction(MenuActionType type, Optional<Integer> targetPage) implements MenuAction {
    public static final Codec<PageAction> NEXT_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("target_page_index").forGetter(PageAction::targetPage)
    ).apply(i, idx -> new PageAction(MenuActionType.NEXT_PAGE, idx)));

    public static final Codec<PageAction> PREVIOUS_CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.optionalFieldOf("target_page_index").forGetter(PageAction::targetPage)
    ).apply(i, idx -> new PageAction(MenuActionType.PREVIOUS_PAGE, idx)));

    // jump_to_page: REQUIRED target_page_index >= 0
    private static final Codec<Integer> NON_NEG_INT = Codec.INT.flatXmap(
        n -> n >= 0 ? DataResult.success(n) : DataResult.error(() -> "target_page_index must be >= 0"),
        DataResult::success
    );

    public static final Codec<PageAction> JUMP_TO_PAGE_CODEC = RecordCodecBuilder.create(i -> i.group(
        NON_NEG_INT.fieldOf("target_page_index")
            .forGetter(pa -> pa.targetPage()
                .orElseThrow(() -> new IllegalStateException("Missing target_page_index for jump_to_page")))
    ).apply(i, idx -> new PageAction(MenuActionType.JUMP_TO_PAGE, Optional.of(idx))));
}
