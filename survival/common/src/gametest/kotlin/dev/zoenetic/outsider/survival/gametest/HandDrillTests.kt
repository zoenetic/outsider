package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
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

// Generous enough for any dust roll to be drilled, with re-presses, and the ember to be nursed.
private const val EMBER_TICKS = 900

// A real in-level server player, so the server's own tick drives Work.tick exactly once a tick.
object HandDrillTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)
    private val OTHER_CAMPFIRE_POS = BlockPos(1, 1, 3)

    private fun GameTestHelper.campfire(lit: Boolean, at: BlockPos = CAMPFIRE_POS) =
        setBlock(at, OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, lit))

    private fun GameTestHelper.isLit(): Boolean = getBlockState(CAMPFIRE_POS).getValue(LIT)

    private fun GameTestHelper.targetOf(pos: BlockPos): Vec3 =
        Vec3.atBottomCenterOf(absolutePos(pos)).add(0.0, 0.2, 0.0)

    /** A survival player two blocks from the campfire, looking at it, holding [main] and [off]. */
    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.worker(
        main: ItemStack = ItemStack(OutsiderItems.HAND_DRILL),
        off: ItemStack = ItemStack.EMPTY,
    ): ServerPlayer {
        val pos = absolutePos(CAMPFIRE_POS)
        val player = makeMockServerPlayerInLevel()
        player.setGameMode(GameType.SURVIVAL)
        player.snapTo(pos.x + 2.5, pos.y.toDouble(), pos.z + 0.5)
        player.lookAt(EntityAnchorArgument.Anchor.EYES, targetOf(CAMPFIRE_POS))
        player.setItemInHand(InteractionHand.MAIN_HAND, main)
        player.setItemInHand(InteractionHand.OFF_HAND, off)
        return player
    }

    private fun GameTestHelper.useOn(
        player: ServerPlayer,
        hand: InteractionHand,
        pos: BlockPos = CAMPFIRE_POS,
    ): InteractionResult {
        val hit = BlockHitResult(targetOf(pos), Direction.EAST, absolutePos(pos), false)
        return player.getItemInHand(hand).useOn(UseOnContext(player, hand, hit))
    }

    // Vanilla's order: the main hand first, the off hand only if the main hand passes.
    private fun GameTestHelper.press(player: ServerPlayer): InteractionResult {
        for (hand in InteractionHand.entries) {
            val result = useOn(player, hand)
            if (result != InteractionResult.PASS) return result
        }
        return InteractionResult.PASS
    }

    private fun workOf(player: ServerPlayer): LightingFireState? =
        Work.get(player) as? LightingFireState

    private fun GameTestHelper.workingOn(player: ServerPlayer): LightingFireState =
        workOf(player) ?: throw assertionException("the player isn't working")

    private fun GameTestHelper.drillingPhase(player: ServerPlayer): Drilling {
        val phase = workingOn(player).phase
        return phase as? Drilling ?: throw assertionException("expected drilling, found $phase")
    }

    private fun GameTestHelper.startDrilling(player: ServerPlayer = worker()): ServerPlayer {
        val result = press(player)
        ensure(result == InteractionResult.CONSUME) {
            "using the drill on an unlit campfire returned $result"
        }
        ensure(player.isUsingItem) { "the player isn't drilling" }
        return player
    }

    // Drill like a player would: press again whenever the use runs out, until the ember forms.
    private fun GameTestHelper.keepDrillingUntilEmber(player: ServerPlayer, onEmber: () -> Unit) {
        var ember = false
        onEachTick {
            if (ember) return@onEachTick
            when (workOf(player)?.phase) {
                is Drilling -> if (!player.isUsingItem) {
                    val _ = press(player)
                }

                is NursingEmbers -> {
                    ember = true
                    onEmber()
                }

                null -> Unit
            }
        }
    }

    fun usingTheDrillStartsWorkOnTheCampfire(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        val work = helper.workingOn(player)
        helper.ensure(work.blockPos == helper.absolutePos(CAMPFIRE_POS)) {
            "work started on ${work.blockPos}, not the campfire"
        }
        val phase = helper.drillingPhase(player)
        helper.ensure(phase.dust == Duration.NONE) { "work started at ${phase.dust.value} dust" }
        helper.ensure(!phase.held) { "work started already held, before any tick" }
        helper.succeed()
    }

    fun aDrillInTheOffHandStartsWork(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling(
            helper.worker(main = ItemStack.EMPTY, off = ItemStack(OutsiderItems.HAND_DRILL)),
        )
        helper.ensure(player.usedItemHand == InteractionHand.OFF_HAND) {
            "drilling with the ${player.usedItemHand}"
        }
        val _ = helper.drillingPhase(player)
        helper.succeed()
    }

    fun aLitCampfireCantBeDrilled(helper: GameTestHelper) {
        helper.campfire(lit = true)
        val player = helper.worker()
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) {
            "using the drill on a lit campfire returned $result"
        }
        helper.ensure(!player.isUsingItem) { "started drilling a lit campfire" }
        helper.ensure(workOf(player) == null) { "started work on a lit campfire" }
        helper.succeed()
    }

    fun tinderCantStartWork(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(main = ItemStack(OutsiderItems.TINDER))
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "tinder alone returned $result" }
        helper.ensure(workOf(player) == null) { "tinder started work without an ember" }
        helper.succeed()
    }

    fun tinderDoesNothingWhileDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        player.releaseUsingItem()
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.TINDER))
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "tinder while drilling returned $result" }
        helper.ensure(!player.isUsingItem) { "tinder went into use before an ember" }
        helper.succeed()
    }

    fun usingTheDrillOnAnotherCampfireDoesNothing(helper: GameTestHelper) {
        helper.campfire(lit = false)
        helper.campfire(lit = false, at = OTHER_CAMPFIRE_POS)
        val player = helper.startDrilling()
        player.releaseUsingItem()
        val before = helper.workingOn(player)
        val result = helper.useOn(player, InteractionHand.MAIN_HAND, OTHER_CAMPFIRE_POS)
        helper.ensure(result == InteractionResult.PASS) {
            "the drill on a second campfire returned $result"
        }
        helper.ensure(!player.isUsingItem) { "the drill went into use on a second campfire" }
        helper.ensure(helper.workingOn(player) == before) { "the work moved to the other campfire" }
        helper.succeed()
    }

    fun drillingFormsAnEmberInOneHold(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        val required = helper.drillingPhase(player).dustRequired.value
        val startedAt = helper.tick
        helper.succeedWhen {
            val phase = helper.workingOn(player).phase
            helper.ensure(phase is NursingEmbers) {
                "no ember after ${helper.tick - startedAt} ticks of one hold " +
                    "(dust needed: $required, still using: ${player.isUsingItem})"
            }
            val took = helper.tick - startedAt
            helper.ensure(took >= required) { "ember after $took ticks, before the rolled $required" }
            helper.ensure(!player.isUsingItem) { "still drilling once the ember formed" }
            helper.ensure(!helper.isLit()) { "lit without blowing on the ember" }
            Work.end(player)
        }
    }

    fun drillingWearsTheDrillOncePerSecond(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drill = ItemStack(OutsiderItems.HAND_DRILL)
        val player = helper.startDrilling(helper.worker(main = drill))
        val required = helper.drillingPhase(player).dustRequired.value
        helper.keepDrillingUntilEmber(player) {
            val expected = (required / 20).toInt()
            helper.ensure(drill.damageValue == expected) {
                "drill took ${drill.damageValue} damage over $required ticks, expected $expected"
            }
            Work.end(player)
            helper.succeed()
        }
    }

    fun releasingPausesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val drill = ItemStack(OutsiderItems.HAND_DRILL)
        val player = helper.startDrilling(helper.worker(main = drill))
        player.releaseUsingItem()
        helper.runAfterDelay(10) {
            val phase = helper.drillingPhase(player)
            helper.ensure(phase.dust == Duration.NONE && !phase.held) {
                "drilling advanced to ${phase.dust.value} without the drill in use"
            }
            helper.ensure(drill.damageValue == 0) { "the drill wore while paused" }
            helper.ensure(!helper.isLit()) { "lit while paused" }
            helper.succeed()
        }
    }

    fun releasingBanksTheDrillingSoFar(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(10) { player.releaseUsingItem() }
        var banked: Drilling? = null
        helper.runAfterDelay(12) {
            val phase = helper.drillingPhase(player)
            helper.ensure(!phase.held) { "still held after releasing" }
            helper.ensure(phase.dust > Duration.NONE) { "releasing banked no dust" }
            helper.ensure(phase.heat > Heat(20.0)) { "releasing banked no heat" }
            banked = phase
        }
        helper.runAfterDelay(20) {
            val phase = helper.drillingPhase(player)
            helper.ensure(phase == banked) { "resting changed $banked to $phase" }
            helper.succeed()
        }
    }

    fun usingTheDrillAgainResumesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(5) {
            val before = helper.drillingPhase(player)
            player.releaseUsingItem()
            val result = helper.press(player)
            helper.ensure(result == InteractionResult.CONSUME) { "using it again returned $result" }
            val after = helper.drillingPhase(player)
            helper.ensure(after == before) { "using it again replaced $before with $after" }
            helper.ensure(player.isUsingItem) { "using it again didn't resume drilling" }
            helper.succeed()
        }
    }

    fun lookingAwayKeepsWorking(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) != null) { "stopped working after looking away" }
            helper.succeed()
        }
    }

    fun lookingAwayPausesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        helper.runAfterDelay(10) {
            val phase = helper.drillingPhase(player)
            helper.ensure(!phase.held && phase.dust == Duration.NONE) {
                "drilled ${phase.dust.value} dust while looking at the sky"
            }
            helper.succeed()
        }
    }

    fun lookingAwayMidDrillBanksOnlyTheDrillingDone(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(10) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        }
        helper.runAfterDelay(40) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.targetOf(CAMPFIRE_POS))
        }
        helper.runAfterDelay(42) {
            val phase = helper.drillingPhase(player)
            helper.ensure(!phase.held) { "still held after looking away" }
            helper.ensure(phase.dust >= Duration(8) && phase.dust <= Duration(11)) {
                "banked ${phase.dust.value} dust from 10 ticks of drilling and 30 of looking away"
            }
            helper.ensure(phase.heat > Heat(20.0)) { "looking away dropped the heat drilled" }
            helper.succeed()
        }
    }

    // Any change to the state is a sync packet, so resting must leave it exactly as it was.
    fun lookingAwayRestsWithoutChangingTheState(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(5) {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))
        }
        var resting: LightingFireState? = null
        helper.runAfterDelay(8) { resting = helper.workingOn(player) }
        helper.runAfterDelay(20) {
            val now = helper.workingOn(player)
            helper.ensure(now == resting) { "resting changed $resting to $now" }
            helper.succeed()
        }
    }

    fun puttingTheDrillAwayRestsWithoutEndingWork(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY)
        helper.runAfterDelay(2) {
            val phase = helper.drillingPhase(player)
            helper.ensure(!phase.held) { "still drilling without the drill in hand" }
            helper.succeed()
        }
    }

    fun anEmberLeftAloneGoesBackToDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        var emberAt: Long? = null
        helper.keepDrillingUntilEmber(player) { emberAt = helper.tick }
        helper.succeedWhen {
            val formed = emberAt ?: throw helper.assertionException("no ember yet")
            helper.ensure(helper.tick > formed) { "the ember only just formed" }
            val phase = helper.drillingPhase(player)
            helper.ensure(phase.dust == Duration.NONE) { "the new attempt kept ${phase.dust.value} dust" }
            helper.ensure(!helper.isLit()) { "lit without blowing on the ember" }
        }
    }

    private fun GameTestHelper.lightsTheCampfire(player: ServerPlayer, onEmber: () -> Unit) {
        val _ = startDrilling(player)
        keepDrillingUntilEmber(player) {
            onEmber()
            val result = press(player)
            ensure(result == InteractionResult.CONSUME) { "using the tinder on the ember returned $result" }
            ensure(player.useItem.`is`(OutsiderItems.TINDER)) { "blowing with ${player.useItem}" }
        }
        succeedWhen {
            ensure(isLit()) { "not lit yet (${workOf(player)?.phase})" }
            ensure(workOf(player) == null) { "still working once the campfire caught" }
        }
    }

    fun drillInTheMainHandAndTinderInTheOffHandLightTheCampfire(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER))
        helper.lightsTheCampfire(player) {}
    }

    fun tinderInTheMainHandAndDrillInTheOffHandLightTheCampfire(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(
            main = ItemStack(OutsiderItems.TINDER),
            off = ItemStack(OutsiderItems.HAND_DRILL),
        )
        helper.lightsTheCampfire(player) {}
    }

    fun swappingOneHandFromDrillToTinderLightsTheCampfire(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker()
        helper.lightsTheCampfire(player) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(OutsiderItems.TINDER))
        }
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "using_the_drill_starts_work_on_the_campfire",
            SHORT_TICKS,
            ::usingTheDrillStartsWorkOnTheCampfire,
        ),
        SurvivalTest(
            "a_drill_in_the_off_hand_starts_work",
            SHORT_TICKS,
            ::aDrillInTheOffHandStartsWork,
        ),
        SurvivalTest("a_lit_campfire_cant_be_drilled", SHORT_TICKS, ::aLitCampfireCantBeDrilled),
        SurvivalTest("tinder_cant_start_work", SHORT_TICKS, ::tinderCantStartWork),
        SurvivalTest(
            "tinder_does_nothing_while_drilling",
            SHORT_TICKS,
            ::tinderDoesNothingWhileDrilling,
        ),
        SurvivalTest(
            "using_the_drill_on_another_campfire_does_nothing",
            SHORT_TICKS,
            ::usingTheDrillOnAnotherCampfireDoesNothing,
        ),
        SurvivalTest(
            "drilling_forms_an_ember_in_one_hold",
            EMBER_TICKS,
            ::drillingFormsAnEmberInOneHold,
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
        SurvivalTest("looking_away_pauses_drilling", SHORT_TICKS, ::lookingAwayPausesDrilling),
        SurvivalTest(
            "looking_away_mid_drill_banks_only_the_drilling_done",
            60,
            ::lookingAwayMidDrillBanksOnlyTheDrillingDone,
        ),
        SurvivalTest(
            "looking_away_rests_without_changing_the_state",
            30,
            ::lookingAwayRestsWithoutChangingTheState,
        ),
        SurvivalTest(
            "putting_the_drill_away_rests_without_ending_work",
            SHORT_TICKS,
            ::puttingTheDrillAwayRestsWithoutEndingWork,
        ),
        SurvivalTest(
            "an_ember_left_alone_goes_back_to_drilling",
            EMBER_TICKS,
            ::anEmberLeftAloneGoesBackToDrilling,
        ),
        SurvivalTest(
            "drill_in_the_main_hand_and_tinder_in_the_off_hand_light_the_campfire",
            EMBER_TICKS,
            ::drillInTheMainHandAndTinderInTheOffHandLightTheCampfire,
        ),
        SurvivalTest(
            "tinder_in_the_main_hand_and_drill_in_the_off_hand_light_the_campfire",
            EMBER_TICKS,
            ::tinderInTheMainHandAndDrillInTheOffHandLightTheCampfire,
        ),
        SurvivalTest(
            "swapping_one_hand_from_drill_to_tinder_lights_the_campfire",
            EMBER_TICKS,
            ::swappingOneHandFromDrillToTinderLightsTheCampfire,
        ),
    )
}
