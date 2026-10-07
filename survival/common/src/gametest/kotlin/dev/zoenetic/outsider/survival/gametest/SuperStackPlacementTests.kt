package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks

private const val SOME = 5
private const val TEST_TICKS = 20
private val GROUND = BlockPos(1, 1, 1)

private fun GameTestHelper.holdingLitTorches(gameType: GameType): Player =
    makeMockPlayer(gameType).apply {
        setItemInHand(InteractionHand.MAIN_HAND, litSuperStackOf(torches(SOME)))
    }

/** Placing from a super stack takes `LIT` off it; a failed placement leaves it unchanged. */
object SuperStackPlacementTests {

    fun placingTheLitTorchLeavesTheStackUnlit(helper: GameTestHelper) {
        helper.setBlock(GROUND, Blocks.STONE)
        val player = helper.holdingLitTorches(GameType.SURVIVAL)

        helper.placeAt(player, player.mainHandItem, GROUND, Direction.UP)

        helper.assertBlockPresent(OutsiderBlocks.TORCH, GROUND.above())
        val held = player.mainHandItem
        helper.ensure(helper.totalIn(held) == SOME - 1 && held.litCount() == 0) {
            "expected ${SOME - 1} unlit torches left, found ${held.groups()}"
        }
        helper.succeed()
    }

    fun placingTheLitTorchInCreativeLeavesTheStackUnlit(helper: GameTestHelper) {
        helper.setBlock(GROUND, Blocks.STONE)
        val player = helper.holdingLitTorches(GameType.CREATIVE)
        player.abilities.instabuild = true

        helper.placeAt(player, player.mainHandItem, GROUND, Direction.UP)

        helper.assertBlockPresent(OutsiderBlocks.TORCH, GROUND.above())
        val held = player.mainHandItem
        helper.ensure(helper.totalIn(held) == SOME && held.litCount() == 0) {
            "expected all $SOME torches kept and unlit, found ${held.groups()}"
        }
        helper.succeed()
    }

    fun aFailedPlacementKeepsTheTorchLit(helper: GameTestHelper) {
        helper.setBlock(GROUND, Blocks.STONE)
        helper.setBlock(GROUND.above(), Blocks.STONE)
        helper.setBlock(GROUND.above(2), Blocks.STONE)
        val player = helper.holdingLitTorches(GameType.SURVIVAL)

        helper.placeAt(player, player.mainHandItem, GROUND, Direction.UP)

        val held = player.mainHandItem
        helper.ensure(helper.totalIn(held) == SOME && held.litCount() == 1) {
            "expected all $SOME torches kept with the lit one still lit, found ${held.groups()}"
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "placing_the_lit_torch_leaves_the_stack_unlit",
            TEST_TICKS,
            ::placingTheLitTorchLeavesTheStackUnlit,
        ),
        SurvivalTest(
            "placing_the_lit_torch_in_creative_leaves_the_stack_unlit",
            TEST_TICKS,
            ::placingTheLitTorchInCreativeLeavesTheStackUnlit,
        ),
        SurvivalTest(
            "a_failed_placement_keeps_the_torch_lit",
            TEST_TICKS,
            ::aFailedPlacementKeepsTheTorchLit,
        ),
    )
}
