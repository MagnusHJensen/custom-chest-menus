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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.customchestmenus.models.BaseItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CraftItemsAction(List<BaseItem> inputs, List<BaseItem> outputs, boolean hideDefaultText, Optional<Component> customMissingInputMessage) implements MenuAction {


    public static final MapCodec<CraftItemsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        BaseItem.CODEC.listOf().fieldOf("inputs").forGetter(CraftItemsAction::inputs),
        BaseItem.CODEC.listOf().fieldOf("outputs").forGetter(CraftItemsAction::outputs),
        Codec.BOOL.optionalFieldOf("hide_text", false).forGetter(CraftItemsAction::hideDefaultText),
        ComponentSerialization.CODEC.optionalFieldOf("missing_input_message").forGetter(CraftItemsAction::customMissingInputMessage)
    ).apply(i, CraftItemsAction::new));

    public record Requirement(ItemStack prototype, int count) {}

    public List<Requirement> requirements() {
        List<Requirement> requirements = new ArrayList<>(inputs.size());

        for (BaseItem input : inputs) {
            ItemStack prototype = input.makeItemStack();
            prototype.setCount(1); // Only item + components take part in matching.

            int existing = indexOfMatching(requirements, prototype);
            if (existing == -1) {
                requirements.add(new Requirement(prototype, input.count()));
            } else {
                Requirement current = requirements.get(existing);
                requirements.set(existing, new Requirement(current.prototype(), current.count() + input.count()));
            }
        }

        return requirements;
    }

    private static int indexOfMatching(List<Requirement> requirements, ItemStack prototype) {
        for (int i = 0; i < requirements.size(); i++) {
            if (ItemStack.isSameItemSameComponents(requirements.get(i).prototype(), prototype)) {
                return i;
            }
        }
        return -1;
    }

    public int maxRepeats(Inventory inv, List<Requirement> requirements) {
        if (requirements.isEmpty()) {
            return 0;
        }

        var highestRepeats = Integer.MAX_VALUE;

        for (Requirement req : requirements) {
            if (req.count() <= 0) {
                return 0; // Invalid requirement count
            }

            int possibleRepeats = available(inv, req.prototype()) / req.count();
            if (possibleRepeats == 0) {
                return 0; // Not enough of this requirement to craft even once
            }

            highestRepeats = Math.min(highestRepeats, possibleRepeats);
        }

        return highestRepeats;
    }

    public Optional<Map<Integer, Integer>> plan(Inventory inv, List<Requirement> requirements, int repeats) {
        if (repeats <= 0) {
            throw new IllegalStateException("Repeats must be positive");
        }

        if (requirements.isEmpty()) {
            throw new IllegalStateException("Requirements must not be empty");
        }

        Map<Integer, Integer> plan = new HashMap<>();
        for (Requirement req : requirements) {
            int remaining = req.count() * repeats;

            var nonEquipmentSlots = inv.items;
            for (int slot = 0; slot < nonEquipmentSlots.size() && remaining > 0; slot++) {
                ItemStack stack = inv.getItem(slot);
                if (ItemStack.isSameItemSameComponents(stack, req.prototype())) {
                    int available = stack.getCount() - plan.getOrDefault(slot, 0);
                    if (available <= 0) continue;

                    int taken = Math.min(available, remaining);
                    plan.merge(slot, taken, Integer::sum);
                    remaining -= taken;
                }
            }

            if (remaining > 0) {
                return Optional.empty(); // Not enough items to satisfy this requirement
            }

        }

        return Optional.of(plan);
    }

    public record Shortfall(Requirement req, int available, int required) {
        public int missing() { return required - available; }
    }

    public Optional<Shortfall> firstShortfall(Inventory inv, List<Requirement> requirements) {
        for (Requirement req : requirements) {
            int required = req.count();
            int available = available(inv, req.prototype());
            if (available < required) {
                return Optional.of(new Shortfall(req, available, required));
            }
        }
        return Optional.empty();
    }

    /**
     * The total number of {@code prototype} held in the inventory.
     * <p>
     * Equipment slots are not valid crafting inputs, so only the main inventory is scanned. Empty
     * slots never match, since {@code isSameItemSameComponents} compares the item first and no
     * prototype is air.
     */
    private int available(Inventory inv, ItemStack prototype) {
        var nonEquipmentSlots = inv.items;

        int available = 0;
        for (int slot = 0; slot < nonEquipmentSlots.size(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, prototype)) {
                available += stack.getCount();
            }
        }
        return available;
    }

    public Component missingMessageFor(Shortfall shortfall) {
        // Goes through the same placeholder, so the item name reaches the client translatable.
        Component missingMessage = Component.literal("%item% is missing. You have %available% but need %required%.");
        if (this.customMissingInputMessage.isPresent()) {
            missingMessage = this.customMissingInputMessage.get();
        }
        return format(missingMessage, Map.of(
            "item",      shortfall.req().prototype().getHoverName(),
            "missing",   Component.literal(String.valueOf(shortfall.missing())),
            "required",  Component.literal(String.valueOf(shortfall.required())),
            "available", Component.literal(String.valueOf(shortfall.available()))
        ));
    }

    private static final Pattern PLACEHOLDER = Pattern.compile("%(\\w+)%");

    /**
     * Substitutes {@code %placeholder%} occurrences in a component template.
     * <p>
     * Only plain text is scanned - a translatable part of the template is kept as it is, since its
     * text is resolved on the client and is not ours to rewrite. The style of every part is carried
     * over onto a wrapper, so the substituted values inherit the formatting they were written with.
     */
    private static MutableComponent format(Component template, Map<String, Component> values) {
        MutableComponent out = Component.empty().setStyle(template.getStyle());

        if (template.getContents() instanceof PlainTextContents plainText) {
            appendSubstituted(out, plainText.text(), values);
        } else {
            out.append(MutableComponent.create(template.getContents()));
        }

        for (Component sibling : template.getSiblings()) {
            out.append(format(sibling, values));
        }

        return out;
    }

    private static void appendSubstituted(MutableComponent out, String text, Map<String, Component> values) {
        Matcher matcher = PLACEHOLDER.matcher(text);
        int last = 0;
        while (matcher.find()) {
            out.append(Component.literal(text.substring(last, matcher.start())));
            Component value = values.get(matcher.group(1));
            out.append(value != null ? value : Component.literal(matcher.group()));  // unknown -> leave as-is
            last = matcher.end();
        }
        out.append(Component.literal(text.substring(last)));
    }

    @Override
    public MenuActionTypeUnified type() {
        return MenuActionTypeUnified.CRAFT_ITEMS;
    }

    @Override
    public MenuActionTypeV1 typeV1() {
        return null;
    }
}