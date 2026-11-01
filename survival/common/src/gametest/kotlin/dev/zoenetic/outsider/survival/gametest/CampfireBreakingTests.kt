package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext

private const val TEST_TICKS = 20

// Enough breaks to tell a binomial's average from its unlucky rolls.
private const val BREAKS = 400

object CampfireBreakingTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)

    private fun GameTestHelper.dropsOf(state: BlockState): List<ItemStack> {
        setBlock(CAMPFIRE_POS, state)
        return Block.getDrops(state, level, absolutePos(CAMPFIRE_POS), null)
    }

    private fun campfireAt(level: Int): BlockState =
        OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(FUEL_LEVEL, level)

    fun aDeadCampfireBreaksInstantly(helper: GameTestHelper) {
        val state = OutsiderBlocks.DEAD_CAMPFIRE.defaultBlockState()
        helper.setBlock(CAMPFIRE_POS, state)
        val hardness = state.getDestroySpeed(helper.level, helper.absolutePos(CAMPFIRE_POS))
        helper.ensure(hardness == 0F) { "a dead campfire has hardness $hardness" }
        helper.succeed()
    }

    fun aDeadCampfireIsAsLowAsItsModel(helper: GameTestHelper) {
        val state = OutsiderBlocks.DEAD_CAMPFIRE.defaultBlockState()
        helper.setBlock(CAMPFIRE_POS, state)
        val pos = helper.absolutePos(CAMPFIRE_POS)
        val outline = state.getShape(helper.level, pos).max(Direction.Axis.Y)
        val collision = state.getCollisionShape(helper.level, pos, CollisionContext.empty())
            .max(Direction.Axis.Y)
        helper.ensure(outline == 5.0 / 16 && collision == 5.0 / 16) {
            "a dead campfire is ${outline * 16} px high to the eye and ${collision * 16} to walk on"
        }
        helper.succeed()
    }

    fun aDeadCampfireDropsOneToFourCharcoal(helper: GameTestHelper) {
        val state = OutsiderBlocks.DEAD_CAMPFIRE.defaultBlockState()
        val counts = (1..BREAKS).map { _ ->
            helper.dropsOf(state).filter { it.`is`(Items.CHARCOAL) }.sumOf { it.count }
        }
        helper.ensure(counts.all { it in 1..4 }) {
            "a dead campfire dropped ${counts.filter { it !in 1..4 }.distinct()} charcoal"
        }
        val ones = counts.count { it == 1 }
        val twos = counts.count { it == 2 }
        val more = counts.count { it > 2 }
        helper.ensure(ones > more && twos > more) {
            "expected mostly 1s and 2s, got $ones ones, $twos twos and $more more"
        }
        helper.ensure(counts.sum() < BREAKS * 2) {
            "$BREAKS dead campfires gave ${counts.sum()} charcoal, the campfire's worth or more"
        }
        helper.succeed()
    }

    fun aBarelyUsedCampfireDropsWhole(helper: GameTestHelper) {
        for (level in Fuel.MAX.level - 1..Fuel.MAX.level) {
            val drops = helper.dropsOf(campfireAt(level))
            helper.ensure(drops.size == 1 && drops.single().`is`(OutsiderItems.CAMPFIRE)) {
                "a campfire at fuel $level dropped $drops"
            }
        }
        helper.succeed()
    }

    fun aBurningDownCampfireDropsItsFuel(helper: GameTestHelper) {
        val state = campfireAt(Fuel.MAX.level - 2)
        var firewood = 0
        repeat(BREAKS / 20) {
            val drops = helper.dropsOf(state)
            helper.ensure(drops.none { it.`is`(OutsiderItems.CAMPFIRE) }) {
                "a campfire at fuel ${Fuel.MAX.level - 2} dropped whole: $drops"
            }
            firewood += drops.filter { it.`is`(OutsiderItems.FIREWOOD) }.sumOf { it.count }
        }
        helper.ensure(firewood > 0) { "a nearly full campfire never gave back firewood" }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "a_dead_campfire_breaks_instantly",
            TEST_TICKS,
            ::aDeadCampfireBreaksInstantly,
        ),
        SurvivalTest(
            "a_dead_campfire_is_as_low_as_its_model",
            TEST_TICKS,
            ::aDeadCampfireIsAsLowAsItsModel,
        ),
        SurvivalTest(
            "a_dead_campfire_drops_one_to_four_charcoal",
            TEST_TICKS,
            ::aDeadCampfireDropsOneToFourCharcoal,
        ),
        SurvivalTest(
            "a_barely_used_campfire_drops_whole",
            TEST_TICKS,
            ::aBarelyUsedCampfireDropsWhole,
        ),
        SurvivalTest(
            "a_burning_down_campfire_drops_its_fuel",
            TEST_TICKS,
            ::aBurningDownCampfireDropsItsFuel,
        ),
    )
}
