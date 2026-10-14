package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

private const val SHORT_TICKS = 20

// Longest roll (160 + 60) plus the ember phase, with room to spare.
private const val LIGHTING_TICKS = 300

// A real in-level server player, so the server's own tick drives Work.tick exactly once a tick.
object HandDrillTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)

    private class DrillUse(
        val player: ServerPlayer,
        val drill: ItemStack,
        val result: InteractionResult,
    )

    private fun GameTestHelper.campfire(lit: Boolean) =
        setBlock(CAMPFIRE_POS, OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, lit))

    private fun GameTestHelper.isLit(): Boolean = getBlockState(CAMPFIRE_POS).getValue(LIT)

    private fun GameTestHelper.aimAtCampfire(player: ServerPlayer) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, campfireTarget())
    }

    private fun GameTestHelper.campfireTarget(): Vec3 =
        Vec3.atBottomCenterOf(absolutePos(CAMPFIRE_POS)).add(0.0, 0.2, 0.0)

    private fun GameTestHelper.useDrill(player: ServerPlayer, drill: ItemStack): InteractionResult {
        val hit = BlockHitResult(campfireTarget(), Direction.EAST, absolutePos(CAMPFIRE_POS), false)
        return drill.useOn(UseOnContext(player, InteractionHand.MAIN_HAND, hit))
    }

    /** A survival player two blocks from the campfire, looking at it, using the drill on it. */
    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.startDrilling(): DrillUse {
        val pos = absolutePos(CAMPFIRE_POS)
        val player = makeMockServerPlayerInLevel()
        player.setGameMode(GameType.SURVIVAL)
        player.snapTo(pos.x + 2.5, pos.y.toDouble(), pos.z + 0.5)
        aimAtCampfire(player)
        val drill = ItemStack(OutsiderItems.HAND_DRILL)
        player.setItemInHand(InteractionHand.MAIN_HAND, drill)
        return DrillUse(player, drill, useDrill(player, drill))
    }

    private fun workOf(player: ServerPlayer): LightingFireWithDrillState? =
        Work.get(player) as? LightingFireWithDrillState

    private fun GameTestHelper.drillingPhase(
        player: ServerPlayer,
    ): LightingFireWithDrillState.Drilling {
        val work = workOf(player) ?: throw assertionException("the player isn't working")
        return work.phase as? LightingFireWithDrillState.Drilling
            ?: throw assertionException("expected the drilling phase, found ${work.phase}")
    }

    private fun GameTestHelper.requireStarted(
        drilling: DrillUse,
    ): LightingFireWithDrillState.Drilling {
        ensure(drilling.result == InteractionResult.CONSUME) {
            "using the drill on an unlit campfire returned ${drilling.result}"
        }
        ensure(drilling.player.isUsingItem) { "the player isn't drilling" }
        return drillingPhase(drilling.player)
    }

    fun usingTheDrillStartsWorkOnTheCampfire(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val phase = helper.requireStarted(drilling)
        val work = workOf(drilling.player)!!
        helper.ensure(work.blockPos == helper.absolutePos(CAMPFIRE_POS)) {
            "work started on ${work.blockPos}, not the campfire"
        }
        helper.ensure(work.hand == InteractionHand.MAIN_HAND) { "work recorded the ${work.hand}" }
        helper.ensure(phase.elapsed == Duration.NONE) { "work started at ${phase.elapsed.value}" }
        helper.succeed()
    }

    fun drillingLightsTheCampfireAfterTheRolledTime(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling).required.value
        val startedAt = helper.tick
        helper.succeedWhen {
            helper.ensure(helper.isLit()) { "not lit yet" }
            val took = helper.tick - startedAt
            helper.ensure(took >= required) { "lit after $took ticks, before the rolled $required" }
            helper.ensure(workOf(drilling.player) == null) { "still working once lit" }
        }
    }

    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling).required.value
        helper.succeedWhen {
            helper.ensure(helper.isLit()) { "not lit yet" }
            val expected = (required / 20).toInt()
            helper.ensure(drilling.drill.damageValue == expected) {
                "drill took ${drilling.drill.damageValue} damage over $required ticks, " +
                    "expected $expected"
            }
        }
    }

    fun releasingPausesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        drilling.player.releaseUsingItem()
        helper.runAfterDelay(10) {
            val phase = helper.drillingPhase(drilling.player)
            helper.ensure(phase.elapsed == Duration.NONE) {
                "drilling advanced to ${phase.elapsed.value} without the drill in use"
            }
            helper.ensure(drilling.drill.damageValue == 0) { "the drill wore while paused" }
            helper.ensure(!helper.isLit()) { "lit while paused" }
            helper.succeed()
        }
    }

    fun usingTheDrillAgainResumesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        helper.runAfterDelay(5) {
            val before = helper.drillingPhase(drilling.player)
            drilling.player.releaseUsingItem()
            val result = helper.useDrill(drilling.player, drilling.drill)
            helper.ensure(result == InteractionResult.CONSUME) { "using it again returned $result" }
            val after = helper.drillingPhase(drilling.player)
            helper.ensure(after == before) { "using it again replaced $before with $after" }
            helper.ensure(drilling.player.isUsingItem) { "using it again didn't resume drilling" }
            helper.succeed()
        }
    }

    fun lookingAwayEndsWorkWithoutLighting(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        val player = drilling.player
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) == null) { "still working after looking away" }
            helper.ensure(!helper.isLit()) { "lit after looking away" }
            helper.succeed()
        }
    }

    fun puttingTheDrillAwayEndsWork(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        drilling.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
        helper.runAfterDelay(2) {
            helper.ensure(workOf(drilling.player) == null) {
                "still working without the drill in hand"
            }
            helper.succeed()
        }
    }

    fun aLitCampfireCantBeDrilled(helper: GameTestHelper) {
        helper.campfire(lit = true)
        val drilling = helper.startDrilling()
        helper.ensure(drilling.result == InteractionResult.PASS) {
            "using the drill on a lit campfire returned ${drilling.result}"
        }
        helper.ensure(!drilling.player.isUsingItem) { "started drilling a lit campfire" }
        helper.ensure(workOf(drilling.player) == null) { "started work on a lit campfire" }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "using_the_drill_starts_work_on_the_campfire",
            SHORT_TICKS,
            ::usingTheDrillStartsWorkOnTheCampfire,
        ),
        SurvivalTest(
            "drilling_lights_the_campfire_after_the_rolled_time",
            LIGHTING_TICKS,
            ::drillingLightsTheCampfireAfterTheRolledTime,
        ),
        SurvivalTest(
            "drilling_wears_the_drill_once_per_second",
            LIGHTING_TICKS,
            ::drillingWearsTheDrillOncePerSecond,
        ),
        SurvivalTest("releasing_pauses_drilling", SHORT_TICKS, ::releasingPausesDrilling),
        SurvivalTest(
            "using_the_drill_again_resumes_drilling",
            SHORT_TICKS,
            ::usingTheDrillAgainResumesDrilling,
        ),
        SurvivalTest(
            "looking_away_ends_work_without_lighting",
            SHORT_TICKS,
            ::lookingAwayEndsWorkWithoutLighting,
        ),
        SurvivalTest(
            "putting_the_drill_away_ends_work",
            SHORT_TICKS,
            ::puttingTheDrillAwayEndsWork,
        ),
        SurvivalTest("a_lit_campfire_cant_be_drilled", SHORT_TICKS, ::aLitCampfireCantBeDrilled),
    )
}
