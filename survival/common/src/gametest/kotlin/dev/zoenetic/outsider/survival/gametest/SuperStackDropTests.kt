package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.superstack.SuperStackItem
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3

private const val FEW = 4
private const val SOME = 5
private const val PART_BURNT = 7
private const val TEST_TICKS = 20
private const val SEARCH_RADIUS = 3.0
private val GROUND = BlockPos(1, 1, 1)

/** Leaving the inventory: Q drops one torch, and super stacks never stay in the world. */
object SuperStackDropTests {

    fun droppingOneTakesTheActiveTorch(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        inventory.setItem(
            inventory.selectedSlot,
            helper.mixedSuperStackOf(full = FEW, burnt = 1, burntFuel = PART_BURNT),
        )

        val dropped = inventory.removeFromSelected(false)

        helper.ensure(dropped.count == 1 && dropped.fuelLevel() == PART_BURNT) {
            "expected the one part-burnt torch dropped, found $dropped"
        }
        helper.ensure(helper.totalIn(inventory.selectedItem) == FEW) {
            "expected $FEW left, found ${inventory.selectedItem.groups()}"
        }
        helper.succeed()
    }

    fun droppingOneFromALitStackDropsTheLitTorch(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        inventory.setItem(inventory.selectedSlot, helper.litSuperStackOf(torches(SOME)))

        val dropped = inventory.removeFromSelected(false)

        val left = inventory.selectedItem
        helper.ensure(dropped.has(OutsiderComponents.LIT) && left.litCount() == 0) {
            "expected the lit torch dropped, found $dropped, leaving ${left.groups()}"
        }
        helper.succeed()
    }

    fun droppingTheLastTorchEmptiesTheSlot(helper: GameTestHelper) {
        val inventory = helper.makeMockPlayer(GameType.SURVIVAL).inventory
        inventory.setItem(inventory.selectedSlot, helper.superStackOf(torches(1)))

        val dropped = inventory.removeFromSelected(false)

        helper.ensure(dropped.count == 1 && inventory.selectedItem.isEmpty) {
            "expected the last torch dropped and an empty slot, found $dropped, " +
                "leaving ${inventory.selectedItem}"
        }
        helper.succeed()
    }

    fun aDroppedSuperStackSplitsIntoPlainTorches(helper: GameTestHelper) {
        helper.setBlock(GROUND, Blocks.STONE)
        val at = helper.absoluteVec(Vec3.atCenterOf(GROUND.above()))
        val stack = helper.mixedSuperStackOf(full = FEW, burnt = 1, burntFuel = PART_BURNT)
        val _ = helper.level.addFreshEntity(ItemEntity(helper.level, at.x, at.y, at.z, stack))

        helper.succeedWhen {
            val items = helper.getEntities(EntityTypes.ITEM, GROUND.above(), SEARCH_RADIUS)
                .map(ItemEntity::getItem)
            helper.ensure(items.none { it.item is SuperStackItem }) {
                "a super stack is still in the world: $items"
            }
            helper.ensure(
                items.sumOf { it.count } == FEW + 1 && items.any { it.fuelLevel() == PART_BURNT },
            ) {
                "expected ${FEW + 1} plain torches including the part-burnt one, found $items"
            }
        }
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "dropping_one_takes_the_active_torch",
            TEST_TICKS,
            ::droppingOneTakesTheActiveTorch,
        ),
        SurvivalTest(
            "dropping_one_from_a_lit_stack_drops_the_lit_torch",
            TEST_TICKS,
            ::droppingOneFromALitStackDropsTheLitTorch,
        ),
        SurvivalTest(
            "dropping_the_last_torch_empties_the_slot",
            TEST_TICKS,
            ::droppingTheLastTorchEmptiesTheSlot,
        ),
        SurvivalTest(
            "a_dropped_super_stack_splits_into_plain_torches",
            TEST_TICKS,
            ::aDroppedSuperStackSplitsIntoPlainTorches,
        ),
    )
}
