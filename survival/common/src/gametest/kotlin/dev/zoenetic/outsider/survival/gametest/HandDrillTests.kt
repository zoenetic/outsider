package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.HearthBoard
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.tools.TinderItem.Companion.isLitTinder
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.BreathingRate
import dev.zoenetic.outsider.survival.vitals.Vitals
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkClosePacketPayload
import dev.zoenetic.outsider.survival.work.workingEyePosition
import dev.zoenetic.outsider.survival.work.yawTowards
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

private const val SHORT_TICKS = 20

// Generous enough for any dust roll to be drilled, with re-presses, and the ember to be nursed.
private const val EMBER_TICKS = 900

// Long enough to nurse the ember, then for the lit tinder to burn out.
private const val LIT_TINDER_TICKS = 1100

// A real in-level server player, so the server's own tick drives Work.tick exactly once a tick.
object HandDrillTests {

    private val CAMPFIRE_POS = BlockPos(1, 1, 1)
    private val OTHER_CAMPFIRE_POS = BlockPos(1, 1, 2)

    private fun GameTestHelper.campfire(lit: Boolean, at: BlockPos = CAMPFIRE_POS) =
        setBlock(at, OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, lit))

    private fun GameTestHelper.isLit(): Boolean = getBlockState(CAMPFIRE_POS).getValue(LIT)

    private fun GameTestHelper.targetOf(pos: BlockPos): Vec3 =
        Vec3.atBottomCenterOf(absolutePos(pos)).add(0.0, 0.2, 0.0)

    // Fabric's empty test structure has no floor, so the board would have nothing to rest on.
    private fun GameTestHelper.layFloor() {
        for (x in 0..3) for (z in 0..2) setBlock(BlockPos(x, 0, z), Blocks.STONE)
    }

    /** A survival player in reach of both campfires, facing the first, holding [main] and [off]. */
    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.worker(
        main: ItemStack = ItemStack(OutsiderItems.HAND_DRILL),
        off: ItemStack = ItemStack.EMPTY,
    ): ServerPlayer {
        layFloor()
        val pos = absolutePos(CAMPFIRE_POS)
        val player = makeMockServerPlayerInLevel()
        player.setGameMode(GameType.SURVIVAL)
        player.snapTo(pos.x + 1.5, pos.y.toDouble(), pos.z + 0.5)
        player.lookAt(EntityAnchorArgument.Anchor.EYES, targetOf(CAMPFIRE_POS))
        player.lookAt(EntityAnchorArgument.Anchor.EYES, HearthBoard.inFrontOf(player))
        // A mock player never moves, so nothing else would ever put it on the ground.
        player.setOnGround(true)
        player.setItemInHand(InteractionHand.MAIN_HAND, main)
        player.setItemInHand(InteractionHand.OFF_HAND, off)
        tickInventoryOf(player)
        return player
    }

    // A mock player's connection is never ticked, so vanilla never runs its inventory tick.
    private fun GameTestHelper.tickInventoryOf(player: ServerPlayer) = onEachTick {
        player.inventory.tick()
        player.offhandItem.inventoryTick(level, player, EquipmentSlot.OFFHAND)
    }

    private fun GameTestHelper.useOn(
        player: ServerPlayer,
        hand: InteractionHand,
        pos: BlockPos = CAMPFIRE_POS,
        face: Direction = Direction.EAST,
    ): InteractionResult {
        val hit = BlockHitResult(targetOf(pos), face, absolutePos(pos), false)
        return player.getItemInHand(hand).useOn(UseOnContext(player, hand, hit))
    }

    // Vanilla's order: the main hand first, the off hand only if the main hand passes; in each
    // hand the item's use on the clicked block, if any, then its use on air.
    private fun GameTestHelper.press(player: ServerPlayer, at: BlockPos? = null): InteractionResult {
        for (hand in InteractionHand.entries) {
            if (at != null) {
                val result = useOn(player, hand, at)
                if (result != InteractionResult.PASS) return result
            }
            val result = player.getItemInHand(hand).use(level, player, hand)
            if (result != InteractionResult.PASS) return result
        }
        return InteractionResult.PASS
    }

    private fun lookAtTheSky(player: ServerPlayer) =
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.eyePosition.add(0.0, 10.0, 0.0))

    private fun GameTestHelper.lookAtTheBoard(player: ServerPlayer) =
        player.lookAt(EntityAnchorArgument.Anchor.EYES, workingOn(player).board)

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
            "using the drill returned $result"
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

    fun usingTheDrillStartsWorkWithTheBoardInFront(helper: GameTestHelper) {
        val player = helper.worker()
        val board = HearthBoard.inFrontOf(player)
        val _ = helper.startDrilling(player)
        val work = helper.workingOn(player)
        helper.ensure(work.board == board) { "the board is at ${work.board}, not $board" }
        val phase = helper.drillingPhase(player)
        helper.ensure(phase.dust == Duration.NONE) { "work started at ${phase.dust.value} dust" }
        helper.ensure(!phase.held) { "work started already held, before any tick" }
        helper.succeed()
    }

    fun usingTheDrillOnABlockStartsWork(helper: GameTestHelper) {
        helper.campfire(lit = true)
        val player = helper.worker()
        val result = helper.press(player, at = CAMPFIRE_POS)
        helper.ensure(result == InteractionResult.CONSUME) { "the drill on a block returned $result" }
        val _ = helper.drillingPhase(player)
        helper.succeed()
    }

    fun startingWorkLeavesThePlayerWhereTheyStand(helper: GameTestHelper) {
        val player = helper.worker()
        val from = player.position()
        val _ = helper.startDrilling(player)
        helper.ensure(player.position() == from) { "the player moved from $from to ${player.position()}" }
        helper.succeed()
    }

    private val UNDER_THE_LEDGE_BOARD = BlockPos(1, 0, 1)

    // Near the west edge of its block, facing west, so the board lands one block over.
    private fun GameTestHelper.workerAtAnEdge(): ServerPlayer {
        val player = worker()
        val block = absolutePos(BlockPos(2, 1, 1))
        player.snapTo(block.x + 0.2, block.y.toDouble(), block.z + 0.5)
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atBottomCenterOf(block.west()))
        player.lookAt(EntityAnchorArgument.Anchor.EYES, HearthBoard.inFrontOf(player))
        val boardOver = BlockPos.containing(HearthBoard.inFrontOf(player)).below()
        ensure(boardOver == absolutePos(UNDER_THE_LEDGE_BOARD)) {
            "the board is over $boardOver, not the block meant to be taken away"
        }
        return player
    }

    fun theDrillCantStartWithTheBoardOverALedge(helper: GameTestHelper) {
        val player = helper.workerAtAnEdge()
        helper.setBlock(UNDER_THE_LEDGE_BOARD, Blocks.AIR)
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "the drill at a ledge returned $result" }
        helper.ensure(workOf(player) == null) { "started work with the board over a ledge" }
        helper.succeed()
    }

    fun losingTheBoardsFootingEndsTheWork(helper: GameTestHelper) {
        val player = helper.startDrilling(helper.workerAtAnEdge())
        helper.setBlock(UNDER_THE_LEDGE_BOARD, Blocks.AIR)
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) == null) { "still working with nothing under the board" }
            helper.succeed()
        }
    }

    fun aDrillThatBreaksEndsTheWork(helper: GameTestHelper) {
        val drill = ItemStack(OutsiderItems.HAND_DRILL)
        drill.damageValue = drill.maxDamage - 1
        val player = helper.startDrilling(helper.worker(main = drill))
        helper.succeedWhen {
            helper.ensure(player.mainHandItem.isEmpty) { "the drill hasn't broken yet" }
            helper.ensure(workOf(player) == null) { "still working with a broken drill" }
        }
    }

    fun theDrillCantStartOffTheGround(helper: GameTestHelper) {
        val player = helper.worker()
        player.setOnGround(false)
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "the drill in mid-air returned $result" }
        helper.ensure(workOf(player) == null) { "started work in mid-air" }
        helper.succeed()
    }

    fun leavingTheGroundEndsTheWork(helper: GameTestHelper) {
        val player = helper.startDrilling()
        player.setOnGround(false)
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) == null) { "still working off the ground" }
            helper.succeed()
        }
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

    fun aUseAnywhereContinuesTheWork(helper: GameTestHelper) {
        helper.campfire(lit = false, at = OTHER_CAMPFIRE_POS)
        val player = helper.startDrilling()
        player.releaseUsingItem()
        val before = helper.workingOn(player)
        val result = helper.press(player, at = OTHER_CAMPFIRE_POS)
        helper.ensure(result == InteractionResult.CONSUME) {
            "the drill on another block returned $result"
        }
        helper.ensure(player.isUsingItem) { "the drill didn't go back into use" }
        helper.ensure(helper.workingOn(player) == before) { "the use replaced $before" }
        helper.succeed()
    }

    fun aUseWhileLookingAwayDoesNothing(helper: GameTestHelper) {
        val player = helper.startDrilling()
        player.releaseUsingItem()
        lookAtTheSky(player)
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "a use looking away returned $result" }
        helper.ensure(!player.isUsingItem) { "the drill went into use while looking away" }
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
            helper.ensure(took >= required) {
                "ember after $took ticks, before the rolled $required"
            }
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
        lookAtTheSky(player)
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) != null) { "stopped working after looking away" }
            helper.succeed()
        }
    }

    fun lookingAwayPausesDrilling(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        lookAtTheSky(player)
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
            lookAtTheSky(player)
        }
        helper.runAfterDelay(40) {
            helper.lookAtTheBoard(player)
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
            lookAtTheSky(player)
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
            helper.ensure(phase.dust == Duration.NONE) {
                "the new attempt kept ${phase.dust.value} dust"
            }
            helper.ensure(!helper.isLit()) { "lit without blowing on the ember" }
        }
    }

    // Drill, then blow on the ember with the tinder until it catches.
    private fun GameTestHelper.untilTheTinderCatches(
        player: ServerPlayer,
        onEmber: () -> Unit = {},
        onCaught: () -> Unit,
    ) {
        val _ = startDrilling(player)
        keepDrillingUntilEmber(player) {
            onEmber()
            val result = press(player)
            ensure(result == InteractionResult.CONSUME) {
                "using the tinder on the ember returned $result"
            }
            ensure(player.useItem.`is`(OutsiderItems.TINDER)) { "blowing with ${player.useItem}" }
        }
        var caught = false
        onEachTick {
            if (caught || InteractionHand.entries.none { player.getItemInHand(it).isLitTinder() }) {
                return@onEachTick
            }
            caught = true
            ensure(workOf(player) == null) { "still working once the tinder caught" }
            onCaught()
        }
    }

    private fun GameTestHelper.lightsTheCampfire(
        player: ServerPlayer,
        andThen: () -> Unit = {},
        onEmber: () -> Unit = {},
    ) {
        untilTheTinderCatches(player, onEmber) {
            ensure(!isLit()) { "the campfire lit before the tinder was put in" }
            player.releaseUsingItem()
            val result = press(player, at = CAMPFIRE_POS)
            ensure(result == InteractionResult.SUCCESS) {
                "using the lit tinder on the campfire returned $result"
            }
        }
        succeedWhen {
            ensure(isLit()) { "not lit yet (${workOf(player)?.phase})" }
            andThen()
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

    // Read from the hand each time: the stack put there isn't always the one that stays there.
    private fun GameTestHelper.ensureTinder(player: ServerPlayer, count: Int, lit: Boolean) {
        val tinder = player.offhandItem
        ensure(tinder.count == count) { "$count tinder expected, found ${tinder.count}" }
        ensure(tinder.has(OutsiderComponents.LIT) == lit) {
            "tinder should${if (lit) "" else "n't"} be lit"
        }
    }

    fun theTinderCatchesAsAWholeStackAndEndsTheWork(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER, 3))
        helper.untilTheTinderCatches(player) {
            helper.ensureTinder(player, count = 3, lit = true)
            val burnsOutAt = player.offhandItem.get(OutsiderComponents.BURNS_OUT_AT)
            helper.ensure(burnsOutAt == Time(helper.level.gameTime) + TinderItem.BURN_TIME) {
                "burns out at ${burnsOutAt?.value}, not ${TinderItem.BURN_TIME.value} from now"
            }
            helper.succeed()
        }
    }

    fun lightingTheCampfireUsesOneTinder(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER, 3))
        helper.lightsTheCampfire(player, andThen = {
            helper.ensureTinder(player, count = 2, lit = false)
            helper.ensure(!player.offhandItem.has(OutsiderComponents.BURNS_OUT_AT)) {
                "the unlit tinder kept its burn-out time"
            }
        })
    }

    fun lightingTheCampfireWithTheLastTinderUsesItUp(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER))
        helper.lightsTheCampfire(player, andThen = {
            helper.ensure(player.offhandItem.isEmpty) { "${player.offhandItem} left in hand" }
        })
    }

    fun litTinderLeftAloneBurnsOut(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER, 3))
        var caught = false
        helper.untilTheTinderCatches(player) {
            player.releaseUsingItem()
            caught = true
        }
        helper.succeedWhen {
            helper.ensure(caught) { "the tinder hasn't caught yet" }
            helper.ensureTinder(player, count = 2, lit = false)
            helper.ensure(workOf(player) == null) { "working again after the tinder burnt out" }
            helper.ensure(!helper.isLit()) { "lit without putting the tinder in" }
        }
    }

    fun holdingTheLitTinderTooLongBurnsItOutInTheHand(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER, 3))
        var caught = false
        helper.untilTheTinderCatches(player) { caught = true }
        helper.succeedWhen {
            helper.ensure(caught) { "the tinder hasn't caught yet" }
            helper.ensureTinder(player, count = 2, lit = false)
            helper.ensure(!helper.isLit()) { "lit without letting go of the tinder" }
        }
    }

    /** A player holding lit tinder in the off hand, as if it had just caught. */
    private fun GameTestHelper.holdingLitTinder(
        count: Int = 3,
        main: ItemStack = ItemStack.EMPTY,
    ): ServerPlayer {
        val tinder = ItemStack(OutsiderItems.TINDER, count)
        TinderItem.light(tinder, Time(level.gameTime))
        return worker(main = main, off = tinder)
    }

    fun litTinderBurnsOutOnItsOwnTimer(helper: GameTestHelper) {
        val player = helper.holdingLitTinder()
        val litAt = helper.level.gameTime
        var lastLit = litAt
        helper.onEachTick {
            if (player.offhandItem.isLitTinder()) lastLit = helper.level.gameTime
        }
        helper.succeedWhen {
            helper.ensureTinder(player, count = 2, lit = false)
            val burnt = lastLit - litAt
            helper.ensure(burnt in TinderItem.BURN_TIME.value - 2..TinderItem.BURN_TIME.value) {
                "burnt for $burnt ticks, not ${TinderItem.BURN_TIME.value}"
            }
        }
    }

    fun litTinderWithoutABurnOutTimeBurnsOut(helper: GameTestHelper) {
        val tinder = ItemStack(OutsiderItems.TINDER, 3)
        tinder.set(OutsiderComponents.LIT, net.minecraft.util.Unit.INSTANCE)
        val player = helper.worker(off = tinder)
        helper.succeedWhen { helper.ensureTinder(player, count = 2, lit = false) }
    }

    private val LIGHTABLE_POS = BlockPos(1, 1, 2)

    // Lit tinder on the block at LIGHTABLE_POS, with nothing to drill or blow on.
    private fun GameTestHelper.litTinderUsedOn(
        state: BlockState,
        face: Direction = Direction.EAST,
    ): Pair<InteractionResult, ServerPlayer> {
        setBlock(LIGHTABLE_POS, state)
        val player = holdingLitTinder()
        return useOn(player, InteractionHand.OFF_HAND, LIGHTABLE_POS, face) to player
    }

    private fun GameTestHelper.ensureLitTinderLights(state: BlockState) {
        val (result, player) = litTinderUsedOn(state)
        ensure(result == InteractionResult.SUCCESS) { "lighting ${state.block} returned $result" }
        ensure(getBlockState(LIGHTABLE_POS).getValue(LIT)) { "${state.block} didn't light" }
        ensureTinder(player, count = 2, lit = false)
        ensure(workOf(player) == null) { "lighting ${state.block} started work" }
        succeed()
    }

    // On the top face by default, where fire can't sit on anything that isn't a full block.
    private fun GameTestHelper.ensureLitTinderPasses(
        state: BlockState,
        what: String,
        face: Direction = Direction.UP,
    ) {
        val (result, player) = litTinderUsedOn(state, face)
        ensure(result == InteractionResult.PASS) { "lit tinder on $what returned $result" }
        ensure(getBlockState(LIGHTABLE_POS) == state) { "lit tinder changed $what" }
        ensureTinder(player, count = 3, lit = true)
        succeed()
    }

    fun litTinderLightsACampfire(helper: GameTestHelper) = helper.ensureLitTinderLights(
        OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, false),
    )

    fun litTinderLightsAFuelledTorch(helper: GameTestHelper) = helper.ensureLitTinderLights(
        OutsiderBlocks.TORCH.defaultBlockState().setValue(LIT, false).setValue(FUEL_LEVEL, 15),
    )

    fun litTinderLightsAFuelledWallTorch(helper: GameTestHelper) = helper.ensureLitTinderLights(
        OutsiderBlocks.WALL_TORCH.defaultBlockState()
            .setValue(HORIZONTAL_FACING, Direction.SOUTH)
            .setValue(LIT, false)
            .setValue(FUEL_LEVEL, 15),
    )

    fun litTinderLeavesASpentTorchDark(helper: GameTestHelper) = helper.ensureLitTinderPasses(
        OutsiderBlocks.TORCH.defaultBlockState().setValue(LIT, false).setValue(FUEL_LEVEL, 0),
        "a spent torch",
    )

    fun litTinderPassesOnALitCampfire(helper: GameTestHelper) = helper.ensureLitTinderPasses(
        OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(LIT, true),
        "a lit campfire",
    )

    fun litTinderLightsACandle(helper: GameTestHelper) =
        helper.ensureLitTinderLights(Blocks.CANDLE.defaultBlockState())

    fun litTinderLightsACandleCake(helper: GameTestHelper) =
        helper.ensureLitTinderLights(Blocks.CANDLE_CAKE.defaultBlockState())

    fun litTinderLeavesAWaterloggedCandleUnlit(helper: GameTestHelper) =
        helper.ensureLitTinderPasses(
            Blocks.CANDLE.defaultBlockState().setValue(WATERLOGGED, true),
            "a waterlogged candle",
        )

    fun litTinderLeavesAWaterloggedCampfireUnlit(helper: GameTestHelper) =
        helper.ensureLitTinderPasses(
            OutsiderBlocks.CAMPFIRE.defaultBlockState()
                .setValue(LIT, false)
                .setValue(WATERLOGGED, true),
            "a waterlogged campfire",
        )

    private fun GameTestHelper.ensureLitTinderSetsFire(state: BlockState, face: Direction) {
        val (result, player) = litTinderUsedOn(state, face)
        ensure(result == InteractionResult.SUCCESS) { "lit tinder on ${state.block} returned $result" }
        val firePos = LIGHTABLE_POS.relative(face)
        ensure(getBlockState(firePos).block is BaseFireBlock) {
            "no fire on ${state.block}, found ${getBlockState(firePos)}"
        }
        ensure(getBlockState(LIGHTABLE_POS) == state) { "lit tinder changed ${state.block}" }
        ensureTinder(player, count = 2, lit = false)
        succeed()
    }

    fun litTinderSetsFireBesideAFlammableBlock(helper: GameTestHelper) =
        helper.ensureLitTinderSetsFire(Blocks.OAK_PLANKS.defaultBlockState(), Direction.EAST)

    fun litTinderSetsFireOnTheGround(helper: GameTestHelper) =
        helper.ensureLitTinderSetsFire(Blocks.DIRT.defaultBlockState(), Direction.UP)

    // Underneath, the fire would have to go into the floor.
    fun litTinderPassesWhereFireCantSit(helper: GameTestHelper) =
        helper.ensureLitTinderPasses(Blocks.STONE.defaultBlockState(), "stone", Direction.DOWN)

    fun theDrillPassesWhileHoldingLitTinder(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.holdingLitTinder(main = ItemStack(OutsiderItems.HAND_DRILL))
        val result = helper.press(player)
        helper.ensure(result == InteractionResult.PASS) { "the drill returned $result" }
        helper.ensure(workOf(player) == null) { "the drill started work beside lit tinder" }
        helper.succeed()
    }

    // Through the player's connection, so the loader's registered receiver handles it.
    private fun GameTestHelper.closeWork(player: ServerPlayer) {
        MockConnections.prepareForPayloads(player)
        player.connection.handleCustomPayload(
            ServerboundCustomPayloadPacket(WorkClosePacketPayload),
        )
        ensure(!player.hasDisconnected()) { "closing work disconnected the player" }
    }

    fun closingWorkEndsIt(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(5) {
            helper.closeWork(player)
            helper.ensure(workOf(player) == null) { "still working after closing" }
        }
        helper.runAfterDelay(10) {
            helper.ensure(workOf(player) == null) { "work came back after closing" }
            helper.ensure(!helper.isLit()) { "closing lit the campfire" }
            helper.succeed()
        }
    }

    fun closingWorkWithoutWorkDoesNothing(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker()
        helper.closeWork(player)
        helper.closeWork(player)
        helper.runAfterDelay(2) {
            helper.ensure(workOf(player) == null) { "closing started work" }
            helper.succeed()
        }
    }

    fun theDrillStartsFreshWorkAfterClosing(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling()
        helper.runAfterDelay(10) { player.releaseUsingItem() }
        helper.runAfterDelay(12) {
            helper.ensure(helper.drillingPhase(player).dust > Duration.NONE) { "no dust banked" }
            helper.closeWork(player)
            val _ = helper.startDrilling(player)
            val phase = helper.drillingPhase(player)
            helper.ensure(phase.dust == Duration.NONE) {
                "the new work kept ${phase.dust.value} dust from the closed one"
            }
            helper.succeed()
        }
    }

    fun closingWorkWhileNursingTheEmberLeavesTheCampfireUnlit(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling(helper.worker(off = ItemStack(OutsiderItems.TINDER)))
        var closedAt: Long? = null
        helper.keepDrillingUntilEmber(player) {
            val _ = helper.press(player)
            helper.closeWork(player)
            closedAt = helper.tick
        }
        helper.succeedWhen {
            val closed = closedAt ?: throw helper.assertionException("no ember yet")
            helper.ensure(helper.tick >= closed + 40) { "only just closed" }
            helper.ensure(workOf(player) == null) { "still working after closing on the ember" }
            helper.ensure(!helper.isLit()) { "lit after closing on the ember" }
        }
    }

    fun blowingCarriesOnWhileLookingAway(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.worker(off = ItemStack(OutsiderItems.TINDER))
        helper.untilTheTinderCatches(
            player,
            onEmber = { helper.runAfterDelay(1) { lookAtTheSky(player) } },
        ) { helper.succeed() }
    }

    fun theBodyFacesTheWorkWhileLookingAway(helper: GameTestHelper) {
        val player = helper.startDrilling()
        val board = helper.workingOn(player).board
        player.yRot += 120F
        player.yHeadRot = player.yRot
        val start = helper.tick
        helper.succeedWhen {
            helper.ensure(helper.tick >= start + 3) { "the body hasn't had time to turn" }
            val off = abs(Mth.wrapDegrees(player.yBodyRot - yawTowards(player.position(), board)))
            helper.ensure(off < 1F) { "the body is ${off}° off the board" }
        }
    }

    // Out of breath, a held blow lasts one short exhale, pauses to inhale, then blows again.
    fun outOfBreathTheBlowPausesToInhale(helper: GameTestHelper) {
        helper.campfire(lit = false)
        val player = helper.startDrilling(helper.worker(off = ItemStack(OutsiderItems.TINDER)))
        val exhale = BreathingRate(50.0).longestExhale()
        helper.keepDrillingUntilEmber(player) {
            val vitals = Vitals.get(player) ?: Vitals.getFirst(Time(helper.level.gameTime))
            Survival.platform.vitals.set(player, vitals.copy(breathingRate = BreathingRate(50.0)))
            val _ = helper.press(player)
        }
        var blowFrom: Long? = null
        var inhaleFrom: Long? = null
        var nextBreathAt: Time? = null
        helper.onEachTick {
            val phase = workOf(player)?.phase as? NursingEmbers ?: return@onEachTick
            val now = helper.level.gameTime
            when {
                phase.held && blowFrom == null -> blowFrom = now
                !phase.held && blowFrom != null && inhaleFrom == null -> {
                    helper.ensure(player.isUsingItem) { "stopped holding the tinder" }
                    val blew = now - blowFrom
                    helper.ensure(blew in 1..exhale.value + 2) {
                        "the blow lasted $blew ticks, past an exhale of ${exhale.value}"
                    }
                    inhaleFrom = now
                    nextBreathAt = phase.nextBreathAt
                }
                phase.held && inhaleFrom != null -> {
                    helper.ensure(Time(now) == nextBreathAt) {
                        "blew again at $now, not at ${nextBreathAt?.value}"
                    }
                    helper.ensure(now > inhaleFrom) { "never paused to inhale" }
                    helper.succeed()
                }
            }
        }
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "out_of_breath_the_blow_pauses_to_inhale",
            EMBER_TICKS,
            ::outOfBreathTheBlowPausesToInhale,
        ),
        SurvivalTest(
            "starting_work_leaves_the_player_where_they_stand",
            SHORT_TICKS,
            ::startingWorkLeavesThePlayerWhereTheyStand,
        ),
        SurvivalTest(
            "the_drill_cant_start_off_the_ground",
            SHORT_TICKS,
            ::theDrillCantStartOffTheGround,
        ),
        SurvivalTest(
            "the_drill_cant_start_with_the_board_over_a_ledge",
            SHORT_TICKS,
            ::theDrillCantStartWithTheBoardOverALedge,
        ),
        SurvivalTest(
            "losing_the_boards_footing_ends_the_work",
            SHORT_TICKS,
            ::losingTheBoardsFootingEndsTheWork,
        ),
        SurvivalTest(
            "leaving_the_ground_ends_the_work",
            SHORT_TICKS,
            ::leavingTheGroundEndsTheWork,
        ),
        SurvivalTest(
            "a_use_while_looking_away_does_nothing",
            SHORT_TICKS,
            ::aUseWhileLookingAwayDoesNothing,
        ),
        SurvivalTest(
            "blowing_carries_on_while_looking_away",
            EMBER_TICKS,
            ::blowingCarriesOnWhileLookingAway,
        ),
        SurvivalTest(
            "the_body_faces_the_work_while_looking_away",
            SHORT_TICKS,
            ::theBodyFacesTheWorkWhileLookingAway,
        ),
        SurvivalTest(
            "using_the_drill_starts_work_with_the_board_in_front",
            SHORT_TICKS,
            ::usingTheDrillStartsWorkWithTheBoardInFront,
        ),
        SurvivalTest(
            "using_the_drill_on_a_block_starts_work",
            SHORT_TICKS,
            ::usingTheDrillOnABlockStartsWork,
        ),
        SurvivalTest(
            "a_drill_in_the_off_hand_starts_work",
            SHORT_TICKS,
            ::aDrillInTheOffHandStartsWork,
        ),
        SurvivalTest("tinder_cant_start_work", SHORT_TICKS, ::tinderCantStartWork),
        SurvivalTest(
            "tinder_does_nothing_while_drilling",
            SHORT_TICKS,
            ::tinderDoesNothingWhileDrilling,
        ),
        SurvivalTest(
            "a_use_anywhere_continues_the_work",
            SHORT_TICKS,
            ::aUseAnywhereContinuesTheWork,
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
        SurvivalTest(
            "the_tinder_catches_as_a_whole_stack_and_ends_the_work",
            EMBER_TICKS,
            ::theTinderCatchesAsAWholeStackAndEndsTheWork,
        ),
        SurvivalTest(
            "lighting_the_campfire_uses_one_tinder",
            EMBER_TICKS,
            ::lightingTheCampfireUsesOneTinder,
        ),
        SurvivalTest(
            "lighting_the_campfire_with_the_last_tinder_uses_it_up",
            EMBER_TICKS,
            ::lightingTheCampfireWithTheLastTinderUsesItUp,
        ),
        SurvivalTest(
            "lit_tinder_left_alone_burns_out",
            LIT_TINDER_TICKS,
            ::litTinderLeftAloneBurnsOut,
        ),
        SurvivalTest(
            "holding_the_lit_tinder_too_long_burns_it_out_in_the_hand",
            LIT_TINDER_TICKS,
            ::holdingTheLitTinderTooLongBurnsItOutInTheHand,
        ),
        SurvivalTest(
            "lit_tinder_burns_out_on_its_own_timer",
            LIT_TINDER_TICKS,
            ::litTinderBurnsOutOnItsOwnTimer,
        ),
        SurvivalTest(
            "lit_tinder_without_a_burn_out_time_burns_out",
            SHORT_TICKS,
            ::litTinderWithoutABurnOutTimeBurnsOut,
        ),
        SurvivalTest(
            "lit_tinder_lights_a_campfire",
            SHORT_TICKS,
            ::litTinderLightsACampfire,
        ),
        SurvivalTest(
            "lit_tinder_lights_a_fuelled_torch",
            SHORT_TICKS,
            ::litTinderLightsAFuelledTorch,
        ),
        SurvivalTest(
            "lit_tinder_lights_a_fuelled_wall_torch",
            SHORT_TICKS,
            ::litTinderLightsAFuelledWallTorch,
        ),
        SurvivalTest(
            "lit_tinder_leaves_a_spent_torch_dark",
            SHORT_TICKS,
            ::litTinderLeavesASpentTorchDark,
        ),
        SurvivalTest(
            "lit_tinder_passes_on_a_lit_campfire",
            SHORT_TICKS,
            ::litTinderPassesOnALitCampfire,
        ),
        SurvivalTest(
            "lit_tinder_lights_a_candle",
            SHORT_TICKS,
            ::litTinderLightsACandle,
        ),
        SurvivalTest(
            "lit_tinder_lights_a_candle_cake",
            SHORT_TICKS,
            ::litTinderLightsACandleCake,
        ),
        SurvivalTest(
            "lit_tinder_leaves_a_waterlogged_candle_unlit",
            SHORT_TICKS,
            ::litTinderLeavesAWaterloggedCandleUnlit,
        ),
        SurvivalTest(
            "lit_tinder_leaves_a_waterlogged_campfire_unlit",
            SHORT_TICKS,
            ::litTinderLeavesAWaterloggedCampfireUnlit,
        ),
        SurvivalTest(
            "lit_tinder_sets_fire_beside_a_flammable_block",
            SHORT_TICKS,
            ::litTinderSetsFireBesideAFlammableBlock,
        ),
        SurvivalTest(
            "lit_tinder_sets_fire_on_the_ground",
            SHORT_TICKS,
            ::litTinderSetsFireOnTheGround,
        ),
        SurvivalTest(
            "lit_tinder_passes_where_fire_cant_sit",
            SHORT_TICKS,
            ::litTinderPassesWhereFireCantSit,
        ),
        SurvivalTest(
            "a_drill_that_breaks_ends_the_work",
            60,
            ::aDrillThatBreaksEndsTheWork,
        ),
        SurvivalTest(
            "the_drill_passes_while_holding_lit_tinder",
            SHORT_TICKS,
            ::theDrillPassesWhileHoldingLitTinder,
        ),
        SurvivalTest("closing_work_ends_it", SHORT_TICKS, ::closingWorkEndsIt),
        SurvivalTest(
            "closing_work_without_work_does_nothing",
            SHORT_TICKS,
            ::closingWorkWithoutWorkDoesNothing,
        ),
        SurvivalTest(
            "the_drill_starts_fresh_work_after_closing",
            SHORT_TICKS,
            ::theDrillStartsFreshWorkAfterClosing,
        ),
        SurvivalTest(
            "closing_work_while_nursing_the_ember_leaves_the_campfire_unlit",
            EMBER_TICKS,
            ::closingWorkWhileNursingTheEmberLeavesTheCampfireUnlit,
        ),
    )
}
