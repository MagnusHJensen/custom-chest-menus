/*
 *     Custom Chest Menus, a Minecraft mod that allows servers to create custom chest menus.
 *     Copyright (c) 2026  legenden (MagnusHJensen)
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

package dk.magnusjensen.customchestmenus;

import net.minecraft.ChatFormatting;
import dk.magnusjensen.customchestmenus.exception.CraftFailedException;
import dk.magnusjensen.customchestmenus.models.BaseItem;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction.Requirement;
import dk.magnusjensen.customchestmenus.models.actions.CraftItemsAction.Shortfall;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for the craft action, written against the split API:
 * <ul>
 *     <li>{@code CraftItemsAction#requirements()} - the per-craft cost, aggregated by prototype,
 *         with no knowledge of any inventory.</li>
 *     <li>{@code action#maxRepeats(Inventory, List)} - how many times the per-craft cost is
 *         affordable, {@code 0} when it is not affordable at all.</li>
 *     <li>{@code action#plan(Inventory, List, int)} - which slots to take the inputs from for
 *         a given number of repeats, empty when the inventory cannot cover it.</li>
 * </ul>
 */
class CraftItemsActionsTest {

    private static final Identifier DIAMOND = Identifier.withDefaultNamespace("diamond");
    private static final Identifier IRON_INGOT = Identifier.withDefaultNamespace("iron_ingot");
    private static final Identifier DIAMOND_BLOCK = Identifier.withDefaultNamespace("diamond_block");
    private static final Identifier STONE = Identifier.withDefaultNamespace("stone");

    @BeforeAll
    static void bootstrap() {
        TestUtils.bootstrapMinecraft();
    }

    private static BaseItem item(Identifier id, int count) {
        return new BaseItem(id, Component.empty(), count, Map.of());
    }

    /** A configured input/output whose custom name comes from the dedicated {@code name} field. */
    private static BaseItem namedItem(Identifier id, int count, String name) {
        return new BaseItem(id, Component.literal(name), count, Map.of());
    }

    /** The same thing, but routed through the components map instead of the {@code name} field. */
    private static BaseItem componentNamedItem(Identifier id, int count, String name) {
        return new BaseItem(id, Component.empty(), count,
            Map.<DataComponentType<?>, Object>of(DataComponents.CUSTOM_NAME, Component.literal(name)));
    }

    private static ItemStack stack(Identifier id, int count) {
        Item entry = BuiltInRegistries.ITEM.getValue(id);
        return new ItemStack(entry, count);
    }

    private static ItemStack namedStack(Identifier id, int count, String name) {
        ItemStack stack = stack(id, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static CraftItemsAction action(List<BaseItem> inputs, List<BaseItem> outputs) {
        return new CraftItemsAction(inputs, outputs, false, Optional.empty());
    }

    /** Same, with a configured {@code missing_input_message} template. */
    private static CraftItemsAction actionWithMessage(List<BaseItem> inputs, Component missingMessage) {
        return new CraftItemsAction(inputs, List.of(item(DIAMOND_BLOCK, 1)), false, Optional.of(missingMessage));
    }

    /** Shorthand for the common case of a plain, unstyled template. */
    private static CraftItemsAction actionWithMessage(List<BaseItem> inputs, String missingMessage) {
        return actionWithMessage(inputs, Component.literal(missingMessage));
    }

    // ---------------------------------------------------------------------------------------
    // requirements() - pure, derived from the config alone.
    // ---------------------------------------------------------------------------------------

    @Test
    void requirementsCarryThePerCraftCostAndAreNotScaled() {
        CraftItemsAction action = action(
            List.of(item(DIAMOND, 8), item(IRON_INGOT, 2)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        List<Requirement> requirements = action.requirements();

        Assertions.assertEquals(2, requirements.size());
        Assertions.assertEquals(8, requirements.get(0).count());
        Assertions.assertEquals(2, requirements.get(1).count());
        Assertions.assertTrue(ItemStack.isSameItemSameComponents(stack(DIAMOND, 1), requirements.get(0).prototype()));
        Assertions.assertTrue(ItemStack.isSameItemSameComponents(stack(IRON_INGOT, 1), requirements.get(1).prototype()));
    }

    @Test
    void twoInputsOfTheSameItemCollapseIntoASingleRequirement() {
        // Listing the same item twice is a valid way to write a recipe, but it must not be counted
        // as two independent requirements - maxRepeats would then divide the same stock twice.
        CraftItemsAction action = action(
            List.of(item(DIAMOND, 32), item(DIAMOND, 32)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        List<Requirement> requirements = action.requirements();

        Assertions.assertEquals(1, requirements.size());
        Assertions.assertEquals(64, requirements.getFirst().count());
    }

    @Test
    void inputsAreAggregatedByPrototypeNotByConfigShape() {
        // Both spellings produce the same ItemStack, so the inventory scan cannot tell them apart
        // and they have to aggregate.
        CraftItemsAction action = action(
            List.of(namedItem(DIAMOND, 32, "Shiny"), componentNamedItem(DIAMOND, 32, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        List<Requirement> requirements = action.requirements();

        Assertions.assertEquals(1, requirements.size());
        Assertions.assertEquals(64, requirements.getFirst().count());
    }

    @Test
    void inputsThatDifferOnlyByComponentsStayDistinctRequirements() {
        CraftItemsAction action = action(
            List.of(item(DIAMOND, 32), namedItem(DIAMOND, 32, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(2, action.requirements().size());
    }

    // ---------------------------------------------------------------------------------------
    // maxRepeats() - how many crafts the inventory can pay for.
    // ---------------------------------------------------------------------------------------

    @Test
    void maxRepeatsCountsStockAcrossEverySlot() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 10));

        CraftItemsAction action = action(List.of(item(DIAMOND, 64)), List.of(item(DIAMOND_BLOCK, 1)));

        // 138 diamonds in stock, 64 per craft.
        Assertions.assertEquals(2, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void maxRepeatsIsLimitedByTheScarcestInput() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(IRON_INGOT, 10));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 8), item(IRON_INGOT, 2)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        // Diamonds allow 8 crafts, iron only 5.
        Assertions.assertEquals(5, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void maxRepeatsDoesNotCountTheSameStockTwiceForDuplicatedInputs() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));

        // 64 diamonds per craft, written as two inputs of 32.
        CraftItemsAction action = action(
            List.of(item(DIAMOND, 32), item(DIAMOND, 32)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(2, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void maxRepeatsIsZeroWhenTheInventoryCannotCoverASingleCraft() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 63));

        CraftItemsAction action = action(List.of(item(DIAMOND, 128)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertEquals(0, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void maxRepeatsIgnoresStacksWhoseComponentsDoNotMatch() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));                    // plain
        inventory.setItem(1, namedStack(DIAMOND, 10, "Shiny"));      // named

        CraftItemsAction action = action(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        // Only the 10 named diamonds are eligible, the 64 plain ones are a different item entirely.
        Assertions.assertEquals(2, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void canCraftAgreesWithMaxRepeats() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction affordable = action(List.of(item(DIAMOND, 64)), List.of(item(DIAMOND_BLOCK, 1)));
        CraftItemsAction tooExpensive = action(List.of(item(DIAMOND, 65)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertTrue(affordable.maxRepeats(inventory, affordable.requirements()) >= 1);

        Assertions.assertEquals(0, tooExpensive.maxRepeats(inventory, tooExpensive.requirements()));
    }

    // ---------------------------------------------------------------------------------------
    // plan() - which slots pay for the craft.
    // ---------------------------------------------------------------------------------------

    @Test
    void planSpansMoreThanOneStackWhenOneSlotCannotCoverTheInput() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 10));

        CraftItemsAction action = action(List.of(item(DIAMOND, 128)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertEquals(
            Map.of(0, 64, 1, 64),
            action.plan(inventory, action.requirements(), 1).orElseThrow()
        );
    }

    @Test
    void planTakesOnlyWhatIsNeededFromTheLastSlotAndLeavesUnrelatedItemsAlone() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(3, stack(DIAMOND, 40));
        inventory.setItem(7, stack(IRON_INGOT, 64));
        inventory.setItem(9, stack(DIAMOND, 40));
        inventory.setItem(20, stack(DIAMOND, 40));

        CraftItemsAction action = action(List.of(item(DIAMOND, 100)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertEquals(
            Map.of(3, 40, 9, 40, 20, 20),
            action.plan(inventory, action.requirements(), 1).orElseThrow()
        );
    }

    @Test
    void planScalesTheCostByTheNumberOfRepeats() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 32)), List.of(item(DIAMOND_BLOCK, 1)));

        // 3 repeats of 32 = 96 diamonds, drained slot by slot.
        Assertions.assertEquals(
            Map.of(0, 64, 1, 32),
            action.plan(inventory, action.requirements(), 3).orElseThrow()
        );
    }

    @Test
    void planDoesNotHandOutTheSameSlotTwiceForDuplicatedInputs() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 64), item(DIAMOND, 64)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(
            Map.of(0, 64, 1, 64),
            action.plan(inventory, action.requirements(), 1).orElseThrow()
        );
    }

    @Test
    void planIsEmptyWhenTheInventoryCannotCoverTheRepeats() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 63));

        CraftItemsAction action = action(List.of(item(DIAMOND, 128)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertEquals(Optional.empty(), action.plan(inventory, action.requirements(), 1));
    }

    @Test
    void planIsEmptyWhenOneRepeatTooManyIsAskedFor() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 32)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertTrue(action.plan(inventory, action.requirements(), 2).isPresent());
        Assertions.assertEquals(Optional.empty(), action.plan(inventory, action.requirements(), 3));
    }

    @Test
    void planAlwaysSucceedsForTheRepeatCountMaxRepeatsReported() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(4, stack(DIAMOND, 17));
        inventory.setItem(11, stack(IRON_INGOT, 30));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 9), item(IRON_INGOT, 4)),
            List.of(item(DIAMOND_BLOCK, 1))
        );
        List<Requirement> requirements = action.requirements();

        int repeats = action.maxRepeats(inventory, requirements);
        Map<Integer, Integer> plan = action.plan(inventory, requirements, repeats).orElseThrow();

        // 81 diamonds -> 9 crafts, 30 iron -> 7 crafts.
        Assertions.assertEquals(7, repeats);
        Assertions.assertEquals(63 + 28, plan.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void planNeverTakesMoreFromASlotThanItHolds() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 12));
        inventory.setItem(1, stack(DIAMOND, 5));

        CraftItemsAction action = action(List.of(item(DIAMOND, 17)), List.of(item(DIAMOND_BLOCK, 1)));

        Map<Integer, Integer> plan = action.plan(inventory, action.requirements(), 1).orElseThrow();

        plan.forEach((slot, taken) ->
            Assertions.assertTrue(taken <= inventory.getItem(slot).getCount(),
                "slot " + slot + " was asked for " + taken + " but holds " + inventory.getItem(slot).getCount()));
    }

    // ---------------------------------------------------------------------------------------
    // craftItems() - the whole thing, single craft and craft-all.
    // ---------------------------------------------------------------------------------------

    @Test
    void craftingConsumesInputsFromEverySlotAndGivesTheOutput() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));
        inventory.setItem(2, stack(DIAMOND, 10));
        inventory.setItem(5, stack(IRON_INGOT, 70));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 128), item(IRON_INGOT, 70)),
            List.of(item(DIAMOND_BLOCK, 2))
        );

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, false);

        Assertions.assertTrue(leftovers.isEmpty());
        Assertions.assertEquals(10, countOf(inventory, DIAMOND)); // Untouched remainder.
        Assertions.assertEquals(0, countOf(inventory, IRON_INGOT));
        Assertions.assertEquals(2, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void aNormalClickCraftsExactlyOnceEvenWithPlentyOfInputs() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 32)), List.of(item(DIAMOND_BLOCK, 1)));

        ActionExecutor.craftItems(inventory, action, false);

        Assertions.assertEquals(96, countOf(inventory, DIAMOND));
        Assertions.assertEquals(1, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void craftAllRepeatsUntilTheInputsRunOut() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 32)), List.of(item(DIAMOND_BLOCK, 1)));

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, true);

        Assertions.assertTrue(leftovers.isEmpty());
        Assertions.assertEquals(0, countOf(inventory, DIAMOND));
        Assertions.assertEquals(4, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void craftAllLeavesTheRemainderThatCannotPayForAnotherCraft() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 50));

        CraftItemsAction action = action(List.of(item(DIAMOND, 20)), List.of(item(DIAMOND_BLOCK, 1)));

        ActionExecutor.craftItems(inventory, action, true);

        Assertions.assertEquals(10, countOf(inventory, DIAMOND));
        Assertions.assertEquals(2, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void craftAllIsLimitedByTheScarcestInputAndLeavesTheRestUntouched() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(IRON_INGOT, 10));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 8), item(IRON_INGOT, 2)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        ActionExecutor.craftItems(inventory, action, true);

        // Iron caps it at 5 crafts: 40 diamonds and all 10 iron are spent.
        Assertions.assertEquals(5, countOf(inventory, DIAMOND_BLOCK));
        Assertions.assertEquals(24, countOf(inventory, DIAMOND));
        Assertions.assertEquals(0, countOf(inventory, IRON_INGOT));
    }

    @Test
    void craftAllScalesTheOutputsAcrossSeveralStacks() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 1)), List.of(item(DIAMOND_BLOCK, 2)));

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, true);

        Assertions.assertTrue(leftovers.isEmpty());
        Assertions.assertEquals(0, countOf(inventory, DIAMOND));
        Assertions.assertEquals(128, countOf(inventory, DIAMOND_BLOCK)); // Spread over two stacks.
    }

    @Test
    void craftAllWithOnlyEnoughForOneCraftBehavesLikeASingleClick() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 20));

        CraftItemsAction action = action(List.of(item(DIAMOND, 20)), List.of(item(DIAMOND_BLOCK, 1)));

        ActionExecutor.craftItems(inventory, action, true);

        Assertions.assertEquals(0, countOf(inventory, DIAMOND));
        Assertions.assertEquals(1, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void craftingOnlyMatchesStacksWithTheSameComponents() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, namedStack(DIAMOND, 10, "Shiny"));

        CraftItemsAction action = action(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        ActionExecutor.craftItems(inventory, action, true);

        // Both named diamonds are spent over 2 crafts, the plain stack is not eligible.
        Assertions.assertEquals(2, countOf(inventory, DIAMOND_BLOCK));
        Assertions.assertEquals(64, inventory.getItem(0).getCount());
        Assertions.assertEquals(inventory.getItem(1).getItem(), Items.DIAMOND_BLOCK);
    }

    // ---------------------------------------------------------------------------------------
    // Leftovers - what comes back is handed to Player#drop, so it has to be stack sized.
    // ---------------------------------------------------------------------------------------

    /** Leaves slot 0 free for the inputs and fills the rest, so the outputs have nowhere to go. */
    private static Inventory fullInventory() {
        Inventory inventory = TestUtils.emptyInventory();
        for (int slot = 1; slot < inventory.getNonEquipmentItems().size(); slot++) {
            inventory.setItem(slot, stack(STONE, 64));
        }
        return inventory;
    }

    @Test
    void leftoversAreSplitIntoStackSizedChunks() {
        Inventory inventory = fullInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 1)), List.of(item(DIAMOND_BLOCK, 100)));

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, false);

        // A single 100-count ItemEntity is not a valid thing to drop into the world.
        Assertions.assertEquals(100, leftovers.stream().mapToInt(ItemStack::getCount).sum());
        leftovers.forEach(leftover ->
            Assertions.assertTrue(leftover.getCount() <= leftover.getMaxStackSize(),
                "leftover of " + leftover.getCount() + " exceeds the max stack size"));
    }

    @Test
    void leftoversKeepTheComponentsOfTheOutput() {
        Inventory inventory = fullInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 1)),
            List.of(namedItem(DIAMOND_BLOCK, 10, "Shiny"))
        );

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, false);

        Assertions.assertEquals(1, leftovers.size());
        Assertions.assertEquals(10, leftovers.getFirst().getCount());
        Assertions.assertTrue(ItemStack.isSameItemSameComponents(
            namedStack(DIAMOND_BLOCK, 1, "Shiny"), leftovers.getFirst()));
    }

    @Test
    void craftAllDropsEverythingThatDoesNotFit() {
        Inventory inventory = fullInventory();
        inventory.setItem(0, stack(DIAMOND, 3));

        CraftItemsAction action = action(List.of(item(DIAMOND, 1)), List.of(item(DIAMOND_BLOCK, 30)));

        List<ItemStack> leftovers = ActionExecutor.craftItems(inventory, action, true);

        // 3 crafts of 30 blocks, with slot 0 freeing up along the way: 64 land there, 26 drop.
        Assertions.assertEquals(0, countOf(inventory, DIAMOND));
        Assertions.assertEquals(64, countOf(inventory, DIAMOND_BLOCK));
        Assertions.assertEquals(26, leftovers.stream().mapToInt(ItemStack::getCount).sum());
        leftovers.forEach(leftover ->
            Assertions.assertTrue(leftover.getCount() <= leftover.getMaxStackSize(),
                "leftover of " + leftover.getCount() + " exceeds the max stack size"));
    }

    // ---------------------------------------------------------------------------------------
    // Guards - a menu can be configured with nonsense, and craft-all is unbounded.
    // ---------------------------------------------------------------------------------------

    @Test
    void anInputlessActionCannotBeCrafted() {
        Inventory inventory = TestUtils.emptyInventory();

        CraftItemsAction action = action(List.of(), List.of(item(DIAMOND_BLOCK, 1)));

        // Without a guard, a min-over-requirements loop reports Integer.MAX_VALUE and a
        // shift-click hands out outputs forever.
        Assertions.assertEquals(0, action.maxRepeats(inventory, action.requirements()));
    }

    @Test
    void anInputWithoutAPositiveCountCannotBeCrafted() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(item(DIAMOND, 0)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertEquals(0, action.maxRepeats(inventory, action.requirements()));
    }

    // ---------------------------------------------------------------------------------------
    // firstShortfall() - what to tell the player, and only the first thing.
    // ---------------------------------------------------------------------------------------

    @Test
    void thereIsNoShortfallWhenEveryInputIsCovered() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));
        inventory.setItem(1, stack(IRON_INGOT, 64));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 8), item(IRON_INGOT, 2)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(Optional.empty(), action.firstShortfall(inventory, action.requirements()));
    }

    @Test
    void theShortfallIsTheFirstMissingInputInConfigOrder() {
        Inventory inventory = TestUtils.emptyInventory();
        // Both inputs fall short, but only the first one is reported.
        inventory.setItem(0, stack(DIAMOND, 2));
        inventory.setItem(1, stack(IRON_INGOT, 1));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 8), item(IRON_INGOT, 2)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Shortfall shortfall = shortfallOf(action, inventory);

        Assertions.assertTrue(ItemStack.isSameItemSameComponents(
            stack(DIAMOND, 1), shortfall.req().prototype()));
        Assertions.assertEquals(2, shortfall.available());
        Assertions.assertEquals(8, shortfall.required());
        Assertions.assertEquals(6, shortfall.missing());
    }

    @Test
    void theShortfallIsMeasuredAgainstASingleCraft() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 30));

        CraftItemsAction action = action(List.of(item(DIAMOND, 32)), List.of(item(DIAMOND_BLOCK, 1)));

        // Never a multiple of the requested repeats - a player only needs to know what one craft costs.
        Assertions.assertEquals(32, shortfallOf(action, inventory).required());
        Assertions.assertEquals(2, shortfallOf(action, inventory).missing());
    }

    @Test
    void theShortfallOfADuplicatedInputIsTheAggregate() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 50));

        CraftItemsAction action = action(
            List.of(item(DIAMOND, 32), item(DIAMOND, 32)),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(64, shortfallOf(action, inventory).required());
        Assertions.assertEquals(14, shortfallOf(action, inventory).missing());
    }

    @Test
    void stacksWithDifferentComponentsDoNotCountTowardsTheShortfall() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));                  // plain, not eligible
        inventory.setItem(1, namedStack(DIAMOND, 2, "Shiny"));

        CraftItemsAction action = action(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(2, shortfallOf(action, inventory).available());
    }

    @Test
    void aMisconfiguredActionIsUncraftableWithoutAnythingBeingMissing() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction inputless = action(List.of(), List.of(item(DIAMOND_BLOCK, 1)));
        CraftItemsAction zeroCount = action(List.of(item(DIAMOND, 0)), List.of(item(DIAMOND_BLOCK, 1)));

        // Both are uncraftable, but nothing is actually missing - the message needs a fallback.
        Assertions.assertEquals(0, inputless.maxRepeats(inventory, inputless.requirements()));
        Assertions.assertEquals(Optional.empty(), inputless.firstShortfall(inventory, inputless.requirements()));

        Assertions.assertEquals(0, zeroCount.maxRepeats(inventory, zeroCount.requirements()));
        Assertions.assertEquals(Optional.empty(), zeroCount.firstShortfall(inventory, zeroCount.requirements()));
    }

    // ---------------------------------------------------------------------------------------
    // missingMessageFor() - the configurable message.
    // ---------------------------------------------------------------------------------------

    @Test
    void theDefaultMessageNamesTheItemAndBothCounts() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = action(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            List.of(item(DIAMOND_BLOCK, 1))
        );

        Assertions.assertEquals(
            "Shiny is missing. You have 3 but need 5.",
            action.missingMessageFor(shortfallOf(action, inventory)).getString()
        );
    }

    @Test
    void aCustomTemplateFillsInEveryPlaceholder() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            "Need %missing% more %item% (%available%/%required%)"
        );

        Assertions.assertEquals(
            "Need 2 more Shiny (3/5)",
            action.missingMessageFor(shortfallOf(action, inventory)).getString()
        );
    }

    @Test
    void anUnknownPlaceholderIsLeftInTheMessage() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            "Missing %item%, ask %owner%"
        );

        // A typo in the config should be visible, not silently blanked out.
        Assertions.assertEquals(
            "Missing Shiny, ask %owner%",
            action.missingMessageFor(shortfallOf(action, inventory)).getString()
        );
    }

    @Test
    void theItemPlaceholderStaysTranslatable() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 3));

        CraftItemsAction action = actionWithMessage(List.of(item(DIAMOND, 5)), "Missing %item%");

        // Flattening the name to a String would pin every player to the server's locale.
        Assertions.assertTrue(hasTranslatableContents(action.missingMessageFor(shortfallOf(action, inventory))),
            "the item name was flattened into a literal");
    }

    @Test
    void theDefaultMessageAlsoKeepsTheItemNameTranslatable() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 3));

        CraftItemsAction action = action(List.of(item(DIAMOND, 5)), List.of(item(DIAMOND_BLOCK, 1)));

        Assertions.assertTrue(hasTranslatableContents(action.missingMessageFor(shortfallOf(action, inventory))),
            "the default message flattened the item name into a literal");
    }

    // ---------------------------------------------------------------------------------------
    // Component templates - what a plain string template could not express.
    // ---------------------------------------------------------------------------------------

    @Test
    void aStyledTemplateKeepsItsStyle() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            Component.literal("Missing %item%").withStyle(ChatFormatting.RED)
        );

        Component message = action.missingMessageFor(shortfallOf(action, inventory));

        Assertions.assertEquals("Missing Shiny", message.getString());
        Assertions.assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED), message.getStyle().getColor());
    }

    @Test
    void placeholdersAreSubstitutedInEverySiblingOfTheTemplate() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            Component.literal("Missing %missing%x ")
                .append(Component.literal("%item%").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" - have %available%"))
        );

        Assertions.assertEquals("Missing 2x Shiny - have 3",
            action.missingMessageFor(shortfallOf(action, inventory)).getString());
    }

    @Test
    void aTranslatableTemplateIsLeftForTheClientToResolve() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            Component.translatable("custom_chest_menus.craft.missing").append(Component.literal(": %item%"))
        );

        Component message = action.missingMessageFor(shortfallOf(action, inventory));

        // The key survives untouched - resolving it is the client's job, not ours.
        Assertions.assertTrue(hasTranslatableContents(message));
        Assertions.assertTrue(message.getString().endsWith(": Shiny"), message.getString());
    }

    // ---------------------------------------------------------------------------------------
    // craftItems() - the failure path the player actually sees.
    // ---------------------------------------------------------------------------------------

    @Test
    void craftingWithoutEnoughInputsThrowsWithTheMissingItemMessage() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, namedStack(DIAMOND, 3, "Shiny"));

        CraftItemsAction action = actionWithMessage(
            List.of(namedItem(DIAMOND, 5, "Shiny")),
            "You are missing %missing%x %item%"
        );

        CraftFailedException thrown = Assertions.assertThrows(CraftFailedException.class,
            () -> ActionExecutor.craftItems(inventory, action, false));

        Assertions.assertEquals("You are missing 2x Shiny", thrown.componentMessage().getString());
        Assertions.assertEquals(3, countOf(inventory, DIAMOND)); // Nothing was consumed.
        Assertions.assertEquals(0, countOf(inventory, DIAMOND_BLOCK));
    }

    @Test
    void aShiftClickWithNothingToCraftFailsTheSameWayAsANormalClick() {
        Inventory inventory = TestUtils.emptyInventory();

        CraftItemsAction action = actionWithMessage(List.of(namedItem(DIAMOND, 5, "Shiny")), "Missing %item%");

        // craftAll derives its repeat count from maxRepeats, so the guard has to fire before that.
        Assertions.assertEquals("Missing Shiny",
            Assertions.assertThrows(CraftFailedException.class,
                () -> ActionExecutor.craftItems(inventory, action, true)).componentMessage().getString());
        Assertions.assertEquals("Missing Shiny",
            Assertions.assertThrows(CraftFailedException.class,
                () -> ActionExecutor.craftItems(inventory, action, false)).componentMessage().getString());
    }

    @Test
    void craftingAMisconfiguredActionThrowsTheFallbackMessage() {
        Inventory inventory = TestUtils.emptyInventory();
        inventory.setItem(0, stack(DIAMOND, 64));

        CraftItemsAction action = action(List.of(), List.of(item(DIAMOND_BLOCK, 1)));

        CraftFailedException thrown = Assertions.assertThrows(CraftFailedException.class,
            () -> ActionExecutor.craftItems(inventory, action, false));

        Assertions.assertEquals("This item cannot be crafted.", thrown.componentMessage().getString());
    }

    @Test
    void theExceptionCarriesANonNullPlainTextMessageForLogs() {
        Inventory inventory = TestUtils.emptyInventory();

        CraftItemsAction action = actionWithMessage(List.of(namedItem(DIAMOND, 5, "Shiny")), "Missing %item%");

        CraftFailedException thrown = Assertions.assertThrows(CraftFailedException.class,
            () -> ActionExecutor.craftItems(inventory, action, false));

        // ActionExecutor logs getMessage() and wraps it in Component.literal on the generic path.
        Assertions.assertEquals("Missing Shiny", thrown.getMessage());
    }

    private static Shortfall shortfallOf(CraftItemsAction action, Inventory inventory) {
        return action.firstShortfall(inventory, action.requirements()).orElseThrow();
    }

    private static boolean hasTranslatableContents(Component component) {
        if (component.getContents() instanceof TranslatableContents) {
            return true;
        }
        return component.getSiblings().stream().anyMatch(CraftItemsActionsTest::hasTranslatableContents);
    }

    private static int countOf(Inventory inventory, Identifier id) {
        Item item = BuiltInRegistries.ITEM.getValue(id);
        int count = 0;
        for (int slot = 0; slot < inventory.getNonEquipmentItems().size(); slot++) {
            ItemStack inSlot = inventory.getItem(slot);
            if (inSlot.is(item)) {
                count += inSlot.getCount();
            }
        }
        return count;
    }
}
