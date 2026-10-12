package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents.DRILL_TIME_REQUIRED
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.HandDrillItem.Companion.MAX_DRILL_TICKS
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

private const val TEST_TICKS = 20

// Drives the drill's item methods tick by tick with an aimed mock player, so the rolled drill
// time, the wear and the release can be checked exactly.
object HandDrillTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)

    private class Drilling(val player: Player, val drill: ItemStack, val result: InteractionResult)

    private fun GameTestHelper.campfire(lit: Boolean) =
        setBlock(CAMPFIRE_POS, OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, lit))

    private fun GameTestHelper.isLit(): Boolean = getBlockState(CAMPFIRE_POS).getValue(LIT)

    /** A survival player two blocks from the campfire, looking at it, starting to drill. */
    private fun GameTestHelper.startDrilling(crouching: Boolean = true): Drilling {
        val pos = absolutePos(CAMPFIRE_POS)
        val target = Vec3.atBottomCenterOf(pos).add(0.0, 0.2, 0.0)
        val player = makeMockPlayer(GameType.SURVIVAL)
        player.snapTo(pos.x + 2.5, pos.y.toDouble(), pos.z + 0.5)
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target)
        player.isShiftKeyDown = crouching
        val drill = ItemStack(OutsiderItems.HAND_DRILL)
        player.setItemInHand(InteractionHand.MAIN_HAND, drill)
        val hit = BlockHitResult(target, Direction.EAST, pos, false)
        val result = drill.useOn(UseOnContext(player, InteractionHand.MAIN_HAND, hit))
        return Drilling(player, drill, result)
    }

    private fun GameTestHelper.tick(drilling: Drilling, elapsed: Int) =
        drilling.drill.onUseTick(level, drilling.player, MAX_DRILL_TICKS - elapsed)

    private fun GameTestHelper.requireStarted(drilling: Drilling): Int {
        ensure(drilling.result == InteractionResult.CONSUME) {
            "using the drill on an unlit campfire returned ${drilling.result}"
        }
        ensure(drilling.player.isUsingItem) { "the player isn't drilling" }
        return drilling.drill.get(DRILL_TIME_REQUIRED)
            ?: throw assertionException("no drill time was rolled")
    }

    fun drillingLightsTheCampfireAtTheRolledTime(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling)
        for (elapsed in 0 until required) helper.tick(drilling, elapsed)
        helper.ensure(!helper.isLit()) { "lit before the rolled $required ticks" }
        helper.tick(drilling, required)
        helper.ensure(helper.isLit()) { "not lit after the rolled $required ticks" }
        helper.succeed()
    }

    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling)
        for (elapsed in 0..required) helper.tick(drilling, elapsed)
        val expected = required / 20
        helper.ensure(drilling.drill.damageValue == expected) {
            "drill took ${drilling.drill.damageValue} damage over $required ticks, expected $expected"
        }
        helper.succeed()
    }

    fun lookingAwayStopsDrillingWithoutLighting(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        helper.tick(drilling, 0)
        val player = drilling.player
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.tick(drilling, 1)
        helper.ensure(!drilling.player.isUsingItem) { "still drilling after looking away" }
        helper.ensure(!helper.isLit()) { "lit after looking away" }
        helper.ensure(!drilling.drill.has(DRILL_TIME_REQUIRED)) { "the rolled drill time was kept" }
        helper.succeed()
    }

    fun aLitCampfireCantBeDrilled(helper: GameTestHelper) {
        helper.campfire(lit = true)
        val drilling = helper.startDrilling()
        helper.ensure(drilling.result == InteractionResult.PASS) {
            "using the drill on a lit campfire returned ${drilling.result}"
        }
        helper.ensure(!drilling.player.isUsingItem) { "started drilling a lit campfire" }
        helper.succeed()
    }

    fun standingCantStartDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling(crouching = false)
        helper.ensure(drilling.result == InteractionResult.PASS) {
            "using the drill while standing returned ${drilling.result}"
        }
        helper.ensure(!drilling.player.isUsingItem) { "started drilling while standing" }
        helper.succeed()
    }

    fun standingUpStopsDrillingWithoutLighting(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        helper.tick(drilling, 0)
        drilling.player.isShiftKeyDown = false
        helper.tick(drilling, 1)
        helper.ensure(!drilling.player.isUsingItem) { "still drilling after standing up" }
        helper.ensure(!helper.isLit()) { "lit after standing up" }
        helper.succeed()
    }

    fun reachingTheLimitKeepsTheDrill(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        val after = drilling.drill.finishUsingItem(helper.level, drilling.player)
        helper.ensure(after.`is`(OutsiderItems.HAND_DRILL)) {
            "the drill became $after at the limit"
        }
        helper.ensure(!after.has(DRILL_TIME_REQUIRED)) { "the rolled drill time was kept" }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "drilling_lights_the_campfire_at_the_rolled_time",
            TEST_TICKS,
            ::drillingLightsTheCampfireAtTheRolledTime,
        ),
        SurvivalTest(
            "drilling_wears_the_drill_once_per_second",
            TEST_TICKS,
            ::drillingWearsTheDrillOncePerSecond,
        ),
        SurvivalTest(
            "looking_away_stops_drilling_without_lighting",
            TEST_TICKS,
            ::lookingAwayStopsDrillingWithoutLighting,
        ),
        SurvivalTest("a_lit_campfire_cant_be_drilled", TEST_TICKS, ::aLitCampfireCantBeDrilled),
        SurvivalTest("standing_cant_start_drilling", TEST_TICKS, ::standingCantStartDrilling),
        SurvivalTest(
            "standing_up_stops_drilling_without_lighting",
            TEST_TICKS,
            ::standingUpStopsDrillingWithoutLighting,
        ),
        SurvivalTest(
            "reaching_the_limit_keeps_the_drill",
            TEST_TICKS,
            ::reachingTheLimitKeepsTheDrill,
        ),
    )
}
