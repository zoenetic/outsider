package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState
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

// Longest dust roll (200 + 50), with room to spare.
private const val EMBER_TICKS = 300

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

    private fun workOf(player: ServerPlayer): LightingFireState? =
        Work.get(player) as? LightingFireState

    private fun GameTestHelper.drillingPhase(player: ServerPlayer): LightingFireState.Drilling {
        val work = workOf(player) ?: throw assertionException("the player isn't working")
        return work.phase as? LightingFireState.Drilling
            ?: throw assertionException("expected the drilling phase, found ${work.phase}")
    }

    private fun GameTestHelper.requireStarted(drilling: DrillUse): LightingFireState.Drilling {
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
        helper.ensure(phase.hand == InteractionHand.MAIN_HAND) { "work recorded the ${phase.hand}" }
        helper.ensure(phase.dust == Duration.NONE) { "work started at ${phase.dust.value} dust" }
        helper.ensure(!phase.held) { "work started already held, before any tick" }
        helper.succeed()
    }

    private fun GameTestHelper.requireEmber(drilling: DrillUse) {
        val work = workOf(drilling.player)
            ?: throw assertionException("stopped working before an ember formed")
        ensure(work.phase is LightingFireState.NursingEmbers) { "no ember yet" }
    }

    // Blowing isn't implemented yet, so end the work once the ember forms.
    fun drillingFormsAnEmberOnceTheDustIsDrilled(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling).dustRequired.value
        val startedAt = helper.tick
        helper.succeedWhen {
            helper.requireEmber(drilling)
            val took = helper.tick - startedAt
            helper.ensure(took >= required) {
                "ember after $took ticks, before the rolled $required"
            }
            helper.ensure(!drilling.player.isUsingItem) { "still drilling once the ember formed" }
            helper.ensure(!helper.isLit()) { "lit without blowing on the ember" }
            Work.end(drilling.player)
        }
    }

    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val required = helper.requireStarted(drilling).dustRequired.value
        helper.succeedWhen {
            helper.requireEmber(drilling)
            val expected = (required / 20).toInt()
            helper.ensure(drilling.drill.damageValue == expected) {
                "drill took ${drilling.drill.damageValue} damage over $required ticks, " +
                    "expected $expected"
            }
            Work.end(drilling.player)
        }
    }

    fun releasingPausesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        drilling.player.releaseUsingItem()
        helper.runAfterDelay(10) {
            val phase = helper.drillingPhase(drilling.player)
            helper.ensure(phase.dust == Duration.NONE && !phase.held) {
                "drilling advanced to ${phase.dust.value} without the drill in use"
            }
            helper.ensure(drilling.drill.damageValue == 0) { "the drill wore while paused" }
            helper.ensure(!helper.isLit()) { "lit while paused" }
            helper.succeed()
        }
    }

    fun releasingBanksTheDrillingSoFar(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        helper.runAfterDelay(10) { drilling.player.releaseUsingItem() }
        var banked: LightingFireState.Drilling? = null
        helper.runAfterDelay(12) {
            val phase = helper.drillingPhase(drilling.player)
            helper.ensure(!phase.held) { "still held after releasing" }
            helper.ensure(phase.dust > Duration.NONE) { "releasing banked no dust" }
            helper.ensure(phase.heat > Heat(20.0)) { "releasing banked no heat" }
            banked = phase
        }
        helper.runAfterDelay(20) {
            val phase = helper.drillingPhase(drilling.player)
            helper.ensure(phase == banked) { "resting changed $banked to $phase" }
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

    fun lookingAwayKeepsWorking(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drilling = helper.startDrilling()
        val _ = helper.requireStarted(drilling)
        val player = drilling.player
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) != null) { "stopped working after looking away" }
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
            "drilling_forms_an_ember_once_the_dust_is_drilled",
            EMBER_TICKS,
            ::drillingFormsAnEmberOnceTheDustIsDrilled,
        ),
        SurvivalTest(
            "drilling_wears_the_drill_once_per_second",
            EMBER_TICKS,
            ::drillingWearsTheDrillOncePerSecond,
        ),
        SurvivalTest("releasing_pauses_drilling", SHORT_TICKS, ::releasingPausesDrilling),
        SurvivalTest(
            "releasing_banks_the_drilling_so_far",
            30,
            ::releasingBanksTheDrillingSoFar,
        ),
        SurvivalTest(
            "using_the_drill_again_resumes_drilling",
            SHORT_TICKS,
            ::usingTheDrillAgainResumesDrilling,
        ),
        SurvivalTest("looking_away_keeps_working", SHORT_TICKS, ::lookingAwayKeepsWorking),
        SurvivalTest(
            "putting_the_drill_away_ends_work",
            SHORT_TICKS,
            ::puttingTheDrillAwayEndsWork,
        ),
        SurvivalTest("a_lit_campfire_cant_be_drilled", SHORT_TICKS, ::aLitCampfireCantBeDrilled),
    )
}
