package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.work.tools.workingBlockTargetOrNull
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.phys.Vec3

private const val TEST_TICKS = 20

object WorkingModeTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)

    private fun GameTestHelper.campfire(lit: Boolean) =
        setBlock(CAMPFIRE_POS, OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, lit))

    /** A survival player two blocks from the campfire, looking at it. */
    private fun GameTestHelper.playerAtCampfire(crouching: Boolean): Player {
        val pos = absolutePos(CAMPFIRE_POS)
        val player = makeMockPlayer(GameType.SURVIVAL)
        player.snapTo(pos.x + 2.5, pos.y.toDouble(), pos.z + 0.5)
        player.lookAt(
            EntityAnchorArgument.Anchor.EYES,
            Vec3.atBottomCenterOf(pos).add(0.0, 0.2, 0.0),
        )
        player.isShiftKeyDown = crouching
        return player
    }

    private fun Player.isWorking() = workingBlockTargetOrNull() != null

    private fun Player.hold(hand: InteractionHand, stack: ItemStack) = setItemInHand(hand, stack)

    fun crouchingWithTheDrillAtAnUnlitCampfireIsReady(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.playerAtCampfire(crouching = true)
        player.hold(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.HAND_DRILL))
        helper.ensure(player.isWorking()) { "not ready to work on an unlit campfire" }
        helper.succeed()
    }

    fun theDrillInTheOffHandCounts(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.playerAtCampfire(crouching = true)
        player.hold(InteractionHand.OFF_HAND, ItemStack(OutsiderItems.HAND_DRILL))
        helper.ensure(player.isWorking()) { "an off-hand drill didn't count" }
        helper.succeed()
    }

    fun standingIsNotReady(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.playerAtCampfire(crouching = false)
        player.hold(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.HAND_DRILL))
        helper.ensure(!player.isWorking()) { "ready to work while standing" }
        helper.succeed()
    }

    fun withoutAWorkingToolIsNotReady(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.playerAtCampfire(crouching = true)
        player.hold(InteractionHand.MAIN_HAND, ItemStack(Items.STICK))
        helper.ensure(!player.isWorking()) { "ready to work holding a stick" }
        helper.succeed()
    }

    fun lookingAwayIsNotReady(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.playerAtCampfire(crouching = true)
        player.hold(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.HAND_DRILL))
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.ensure(!player.isWorking()) { "ready to work while looking at the sky" }
        helper.succeed()
    }

    // The drill can't be used on a lit campfire, so the mode mustn't lock the player there either.
    fun aLitCampfireIsNotReady(helper: GameTestHelper) {
        helper.campfire(lit = true)
        val player = helper.playerAtCampfire(crouching = true)
        player.hold(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.HAND_DRILL))
        helper.ensure(!player.isWorking()) { "ready to drill a lit campfire" }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "crouching_with_the_drill_at_an_unlit_campfire_is_ready",
            TEST_TICKS,
            ::crouchingWithTheDrillAtAnUnlitCampfireIsReady,
        ),
        SurvivalTest("the_drill_in_the_off_hand_counts", TEST_TICKS, ::theDrillInTheOffHandCounts),
        SurvivalTest("standing_is_not_ready", TEST_TICKS, ::standingIsNotReady),
        SurvivalTest(
            "without_a_working_tool_is_not_ready",
            TEST_TICKS,
            ::withoutAWorkingToolIsNotReady,
        ),
        SurvivalTest("looking_away_is_not_ready", TEST_TICKS, ::lookingAwayIsNotReady),
        SurvivalTest("a_lit_campfire_is_not_ready", TEST_TICKS, ::aLitCampfireIsNotReady),
    )
}
