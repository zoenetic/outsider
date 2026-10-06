package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import dev.zoenetic.outsider.survival.superstack.asSuperStackOrNull
import dev.zoenetic.outsider.survival.superstack.moveIntoSuperStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.util.Unit
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks

private const val FEW = 4
private const val SOME = 5
private const val MANY = 10
private const val PART_BURNT = 7
private const val PLAIN_SLOT = 5
private const val SUPER_STACK_SLOT = 3
private const val TEST_TICKS = 20

private fun torches(count: Int, fuel: Int = Fuel.MAX.level, lit: Boolean = false): ItemStack =
    ItemStack(OutsiderItems.TORCH, count).apply {
        val _ = set(OutsiderComponents.FUEL_LEVEL, Fuel(fuel))
        if (lit) {
            val _ = set(OutsiderComponents.LIT, Unit.INSTANCE)
        }
    }

private fun GameTestHelper.superStackOf(plain: ItemStack): ItemStack = plain.moveIntoSuperStack()
    ?: throw assertionException("expected $plain to wrap into a super stack")

private fun ItemStack.groups(): List<ItemStackTemplate> = getOrDefault(
    BUNDLE_CONTENTS,
    BundleContents.EMPTY,
).items()

private fun GameTestHelper.totalIn(stack: ItemStack): Int = stack.asSuperStackOrNull()?.count
    ?: throw assertionException("expected a super stack, found $stack")

object SuperStackTests {

    fun aLitTorchEnteringASuperStackIsSnuffed(helper: GameTestHelper) {
        val container = helper.superStackOf(torches(1, lit = true))
        if (container.groups().any { it.get(OutsiderComponents.LIT) != null }) {
            throw helper.assertionException("a lit torch should be snuffed on entering")
        }
        if (container.has(OutsiderComponents.LIT)) {
            throw helper.assertionException("the mirror still shows the container as lit")
        }
        helper.succeed()
    }

    fun lightingASuperStackLightsExactlyOneTorch(helper: GameTestHelper) {
        val container = helper.superStackOf(torches(SOME))
        val _ = container.set(OutsiderComponents.LIT, Unit.INSTANCE)

        val lit = container.groups()
            .filter { it.get(OutsiderComponents.LIT) != null }
            .sumOf { it.count }
        val total = helper.totalIn(container)
        if (lit != 1 || total != SOME) {
            throw helper.assertionException(
                "expected 1 of $SOME torches lit, found $lit lit of $total",
            )
        }
        if (!container.has(OutsiderComponents.LIT)) {
            throw helper.assertionException("the lit torch should be active and mirrored")
        }
        helper.succeed()
    }

    fun pickupMergesIntoAnExistingSuperStack(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        inventory.setItem(SUPER_STACK_SLOT, helper.superStackOf(torches(MANY)))
        val incoming = torches(SOME, fuel = PART_BURNT)

        if (!inventory.add(incoming) || !incoming.isEmpty) {
            throw helper.assertionException("pickup should take every torch, left $incoming")
        }
        val occupied = inventory.count { !it.isEmpty }
        val total = helper.totalIn(inventory.getItem(SUPER_STACK_SLOT))
        if (occupied != 1 || total != MANY + SOME) {
            throw helper.assertionException(
                "expected one super stack of ${MANY + SOME}, found $total over $occupied slots",
            )
        }
        helper.succeed()
    }

    fun pickupWithNoSuperStackWrapsTheTorches(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        if (!inventory.add(torches(MANY))) {
            throw helper.assertionException("pickup into an empty inventory should succeed")
        }
        val occupied = inventory.filter { !it.isEmpty }
        if (occupied.size != 1 || helper.totalIn(occupied.single()) != MANY) {
            throw helper.assertionException("expected one super stack of $MANY, found $occupied")
        }
        helper.succeed()
    }

    fun pickupIntoAFullInventoryLeavesTheTorches(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        for (slot in 0 until Inventory.INVENTORY_SIZE) {
            inventory.setItem(slot, ItemStack(Items.DIRT, Items.DIRT.defaultMaxStackSize))
        }
        val incoming = torches(MANY)
        if (inventory.add(incoming) || incoming.count != MANY) {
            throw helper.assertionException("a full inventory should take nothing, left $incoming")
        }
        helper.succeed()
    }

    fun aPlainTorchInTheInventoryIsWrappedInPlace(helper: GameTestHelper) {
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        val inventory = player.inventory
        inventory.setItem(SUPER_STACK_SLOT, helper.superStackOf(torches(MANY)))
        inventory.setItem(PLAIN_SLOT, torches(FEW))

        inventory.getItem(PLAIN_SLOT).inventoryTick(helper.level, player, null)

        val wrapped = helper.totalIn(inventory.getItem(PLAIN_SLOT))
        val existing = helper.totalIn(inventory.getItem(SUPER_STACK_SLOT))
        if (wrapped != FEW || existing != MANY) {
            throw helper.assertionException(
                "expected $FEW wrapped in place beside $MANY, found $wrapped beside $existing",
            )
        }
        helper.succeed()
    }

    fun placingTheLastTorchEmptiesTheHand(helper: GameTestHelper) {
        val ground = BlockPos(1, 1, 1)
        helper.setBlock(ground, Blocks.STONE)
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        player.setItemInHand(InteractionHand.MAIN_HAND, helper.superStackOf(torches(1)))

        helper.placeAt(player, player.mainHandItem, ground, Direction.UP)

        helper.assertBlockPresent(OutsiderBlocks.TORCH, ground.above())
        if (!player.mainHandItem.isEmpty) {
            throw helper.assertionException(
                "placing the last torch left ${player.mainHandItem} in hand",
            )
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_lit_torch_entering_a_super_stack_is_snuffed",
            TEST_TICKS,
            ::aLitTorchEnteringASuperStackIsSnuffed,
        ),
        SurvivalTest(
            "lighting_a_super_stack_lights_exactly_one_torch",
            TEST_TICKS,
            ::lightingASuperStackLightsExactlyOneTorch,
        ),
        SurvivalTest(
            "pickup_merges_into_an_existing_super_stack",
            TEST_TICKS,
            ::pickupMergesIntoAnExistingSuperStack,
        ),
        SurvivalTest(
            "pickup_with_no_super_stack_wraps_the_torches",
            TEST_TICKS,
            ::pickupWithNoSuperStackWrapsTheTorches,
        ),
        SurvivalTest(
            "pickup_into_a_full_inventory_leaves_the_torches",
            TEST_TICKS,
            ::pickupIntoAFullInventoryLeavesTheTorches,
        ),
        SurvivalTest(
            "a_plain_torch_in_the_inventory_is_wrapped_in_place",
            TEST_TICKS,
            ::aPlainTorchInTheInventoryIsWrappedInPlace,
        ),
        SurvivalTest(
            "placing_the_last_torch_empties_the_hand",
            TEST_TICKS,
            ::placingTheLastTorchEmptiesTheHand,
        ),
    )
}
