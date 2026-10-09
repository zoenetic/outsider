package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.fire.FireInteractions
import dev.zoenetic.outsider.survival.fuel.Burnout
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelValues
import dev.zoenetic.outsider.survival.fuel.FuelledBlock
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT

private const val TEST_TICKS = 1000
private const val PART_BURNT_FUEL = 5

/** Ticks into a step, so the fire has a partial step left when it's refuelled. */
private const val MID_STEP = 100L

object RefuelTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)
    private val BURN_RATE = (OutsiderBlocks.CAMPFIRE as FuelledBlock).burnRate
    private val FIREWOOD_FUEL = FuelValues.get(OutsiderItems.FIREWOOD)

    private fun campfire(lit: Boolean): BlockState = OutsiderBlocks.CAMPFIRE.defaultBlockState()
        .setValue(LIT, lit)
        .setValue(FUEL_LEVEL, PART_BURNT_FUEL)

    private fun GameTestHelper.now() = Time(level.gameTime)

    private fun GameTestHelper.stored(): Burnout? {
        val pos = absolutePos(CAMPFIRE_POS)
        return EmitterIndex.burnoutAtPos(level.getChunkAt(pos), pos)
    }

    private fun GameTestHelper.fuelLevel(): Int = getBlockState(CAMPFIRE_POS).getValue(FUEL_LEVEL)

    private fun GameTestHelper.refuelWithFirewood(): InteractionResult {
        val player = makeMockPlayer(GameType.SURVIVAL)
        val pos = absolutePos(CAMPFIRE_POS)
        return FireInteractions.maybeRefuel(
            level.getBlockState(pos),
            ItemStack(OutsiderItems.FIREWOOD),
            level,
            pos,
            player,
        )
    }

    fun refuellingALitCampfireKeepsItsPartialStep(helper: GameTestHelper) {
        helper.setBlock(CAMPFIRE_POS, campfire(lit = true))
        helper.runAfterDelay(MID_STEP) {
            val before = helper.stored() ?: throw helper.assertionException("not in the index")
            val _ = helper.refuelWithFirewood()
            val after = helper.stored() ?: throw helper.assertionException("left the index")
            val expected = before.time + BURN_RATE * FIREWOOD_FUEL.level
            helper.ensure(after.time == expected) {
                "refuelled burnout is ${after.time}, expected $expected (from ${before.time})"
            }
            val level = after.fuelAt(helper.now(), BURN_RATE).level
            helper.ensure(helper.fuelLevel() == level) {
                "fuel level is ${helper.fuelLevel()}, the burnout says $level"
            }
            helper.succeed()
        }
    }

    fun refuellingAnUnlitCampfireAddsToItsLevel(helper: GameTestHelper) {
        helper.setBlock(CAMPFIRE_POS, campfire(lit = false))
        val result = helper.refuelWithFirewood()
        helper.ensure(result != InteractionResult.PASS) { "an unlit campfire refused firewood" }
        val expected = (PART_BURNT_FUEL + FIREWOOD_FUEL.level).coerceAtMost(Fuel.MAX.level)
        helper.ensure(helper.fuelLevel() == expected) {
            "fuel level is ${helper.fuelLevel()}, expected $expected"
        }
        // An unlit fire isn't burning, so it must not be in the index: the dispatch would drain it.
        helper.ensure(helper.stored() == null) { "an unlit campfire is in the emitter index" }
        helper.succeed()
    }

    fun aRefuelledCampfireDropsAtItsNextStep(helper: GameTestHelper) {
        helper.setBlock(CAMPFIRE_POS, campfire(lit = true))
        helper.runAfterDelay(MID_STEP) {
            val _ = helper.refuelWithFirewood()
            val refuelled = helper.fuelLevel()
            val burnout = helper.stored() ?: throw helper.assertionException("left the index")
            val dropAt = burnout.nextDropAt(helper.now(), BURN_RATE)
                ?: throw helper.assertionException("a refuelled campfire has no next drop")
            val delay = (dropAt - helper.now()).value
            helper.runAfterDelay(delay - 1) {
                helper.ensure(helper.fuelLevel() == refuelled) { "dropped before $dropAt" }
            }
            helper.runAfterDelay(delay + 1) {
                helper.ensure(helper.fuelLevel() == refuelled - 1) {
                    "fuel level is ${helper.fuelLevel()} after $dropAt, expected ${refuelled - 1}"
                }
                helper.succeed()
            }
        }
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "refuelling_a_lit_campfire_keeps_its_partial_step",
            TEST_TICKS,
            ::refuellingALitCampfireKeepsItsPartialStep,
        ),
        SurvivalTest(
            "refuelling_an_unlit_campfire_adds_to_its_level",
            TEST_TICKS,
            ::refuellingAnUnlitCampfireAddsToItsLevel,
        ),
        SurvivalTest(
            "a_refuelled_campfire_drops_at_its_next_step",
            TEST_TICKS,
            ::aRefuelledCampfireDropsAtItsNextStep,
        ),
    )
}
