package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.tools.drill.handdrill.DrillingConditions
import dev.zoenetic.outsider.survival.tools.drill.handdrill.HandDrillItem
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.vitals.BLOWING
import dev.zoenetic.outsider.survival.vitals.BreathingRate
import dev.zoenetic.outsider.survival.vitals.HAND_DRILLING
import dev.zoenetic.outsider.survival.vitals.INHALE
import dev.zoenetic.outsider.survival.vitals.MET
import dev.zoenetic.outsider.survival.vitals.Vitals
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.WorkContext
import dev.zoenetic.outsider.survival.work.WorkEffectsContext
import dev.zoenetic.outsider.survival.work.WorkState
import dev.zoenetic.outsider.survival.work.workingEyePosition
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.cos

private const val LOOK_CONE_DEGREES: Double = 30.0
private val BOARD_FOOTING: AABB = AABB(-0.1, -0.05, -0.1, 0.1, 0.0, 0.1)

private val DRILLING_RISE: Heat = Heat(480.0)
private val DRILL_HEAT_HALF_LIFE: Duration = Duration(40)
private val CHAR_IGNITION: Heat = Heat(400.0)
private val SMOKE_FROM: Heat = Heat(200.0)
private val EMBER_BLOWING_RISE: Heat = Heat(800.0)
private val EMBER_HALF_LIFE: Duration = Duration(40)
private val EMBER_DIES: Heat = Heat(250.0)
private val EMBER_GRACE: Duration = Duration(60)
private val TINDER_CATCHES: Heat = Heat(600.0)

private const val EMBER_SMOKE: Double = 0.5
private const val SMOKE_SPEED: Double = 0.02

public fun emberProgress(phase: LightingFireState.NursingEmbers): Float =
    ((phase.heat - EMBER_DIES) / (TINDER_CATCHES - EMBER_DIES)).toFloat().coerceIn(0F, 1F)

public class LightingFire(id: Identifier) : Work<LightingFireState>(id) {

    override val stateCodec: StreamCodec<RegistryFriendlyByteBuf, LightingFireState>
        get() = LightingFireState.STREAM_CODEC

    private fun accepts(player: Player, item: Item): Boolean = when (val current = get(player)) {
        is LightingFireState -> when (current.phase) {
            is LightingFireState.Drilling -> item is HandDrillItem && player.isFacing(current.board)
            is LightingFireState.NursingEmbers -> item is TinderItem
        }

        is WorkState -> false

        null -> item is HandDrillItem && player.canWorkOn(HearthBoard.inFrontOf(player))
    }

    override fun anchor(state: LightingFireState): Vec3 = state.board

    override fun tick(context: WorkContext, state: LightingFireState): LightingFireState? {
        if (!context.player.canWorkOn(state.board)) return null
        when (val phase = state.phase) {
            is LightingFireState.Drilling -> {
                val phase = tickDrilling(context, state.board, phase) ?: return null
                return state.copy(phase = phase)
            }

            is LightingFireState.NursingEmbers -> {
                val phase = tickNursingEmbers(context, state.board, phase) ?: return null
                return state.copy(phase = phase)
            }
        }
    }

    override fun use(player: Player, hand: InteractionHand, item: Item): InteractionResult {
        if (!accepts(player, item)) return InteractionResult.PASS
        if (get(player) == null && player is ServerPlayer) {
            val board = HearthBoard.inFrontOf(player)
            val _ =
                start(player, LightingFireState(board, drilling(WorkContext.of(player))))
                    ?: return InteractionResult.PASS
        }
        player.startUsingItem(hand)
        return InteractionResult.CONSUME
    }

    private fun Player.canWorkHere(): Boolean =
        onGround() && onGround() && !isInWater && !abilities.flying

    private fun Player.canWorkOn(board: Vec3): Boolean =
        canWorkHere() && !level().noCollision(BOARD_FOOTING.move(board))

    private fun Player.isFacing(at: Vec3): Boolean {
        val toward = at.subtract(workingEyePosition()).normalize()
        return lookAngle.dot(toward) >= cos(Math.toRadians(LOOK_CONE_DEGREES))
    }

    private fun tickDrilling(
        context: WorkContext,
        board: Vec3,
        phase: LightingFireState.Drilling,
    ): LightingFireState.Phase? {
        val player = context.player
        val now = context.now
        val conditions = conditions(context.player)
        val dust = phase.dustAt(now)
        val heat = phase.heatAt(now, conditions)
        val hand = player.handWorkingOn<HandDrillItem>(board)
        val held = hand != null
        if (held != phase.held) {
            return phase.copy(dust = dust, heat = heat, held = held, timestamp = now)
        }
        if (!held) return phase
        if (dust > Duration.NONE && dust.value % HandDrillItem.TICKS_PER_WEAR == 0L) {
            val drill = player.getItemInHand(hand)
            drill.hurtAndBreak(1, player, hand)
            if (drill.isEmpty) return null
        }
        if (dust >= phase.dustRequired && heat >= conditions.ignition) {
            player.stopUsingItem()
            return nursingEmbers(context, heat)
        }
        return phase
    }

    private fun tickNursingEmbers(
        context: WorkContext,
        board: Vec3,
        phase: LightingFireState.NursingEmbers,
    ): LightingFireState.Phase? {
        val player = context.player
        val now = context.now
        if (!phase.held && now >= phase.diesAt) return drilling(context)
        val conditions = emberConditions(player)
        val blown = phase.blownAt(now)
        val heat = phase.heatAt(now, conditions)
        val hand = player.handUsing<TinderItem>()
        val blowing = hand != null &&
            if (phase.held) now < phase.breathUntil else now >= phase.nextBreathAt
        if (blowing != phase.held) {
            if (blowing) {
                player.level().playSound(
                    null,
                    BlockPos.containing(board),
                    OutsiderSounds.BLOWING,
                    SoundSource.PLAYERS,
                )
            }
            return phase.copy(
                heat = heat,
                diesAt = if (blowing) phase.diesAt else conditions.diesAt(heat, now),
                blown = blown,
                breathUntil = if (blowing) now + exhale(player) else phase.breathUntil,
                nextBreathAt = if (blowing) phase.nextBreathAt else now + INHALE,
                held = blowing,
                timestamp = now,
            )
        }
        if (!blowing) return phase
        if (blown >= phase.blownRequired && heat >= conditions.catches) {
            TinderItem.light(player.getItemInHand(hand), now)
            return null
        }
        return phase
    }

    private fun emberConditions(player: ServerPlayer): EmberConditions = EmberConditions(
        ambient = (PlayerConditions.get(player) ?: PlayerConditions.EMPTY).ambient,
        blowingRise = EMBER_BLOWING_RISE,
        halfLife = EMBER_HALF_LIFE,
        dies = EMBER_DIES,
        catches = TINDER_CATCHES,
    )

    private fun exhale(player: ServerPlayer): Duration = (
        Vitals.get(player)?.breathingRate
            ?: BreathingRate()
        ).longestExhale()

    override fun tickEffects(context: WorkEffectsContext, state: LightingFireState) {
        val serverPlayer = context.player as? ServerPlayer
        when (val phase = state.phase) {
            is LightingFireState.Drilling -> {
                if (phase.held && phase.dustAt(context.now).value % 7L == 0L) {
                    context.level.playSound(
                        context.player,
                        BlockPos.containing(state.board),
                        OutsiderSounds.DRILL_USE,
                        SoundSource.BLOCKS,
                    )
                }
                if (serverPlayer != null) {
                    val heat = phase.heatAt(context.now, conditions(serverPlayer))
                    serverPlayer.smoke(
                        ((heat - SMOKE_FROM) / (CHAR_IGNITION - SMOKE_FROM)).coerceIn(0.0, 1.0),
                    )
                }
            }

            is LightingFireState.NursingEmbers -> {
                serverPlayer?.smoke(EMBER_SMOKE)
            }
        }
    }

    private fun ServerPlayer.smoke(chance: Double) {
        if (random.nextDouble() >= chance) return
        val at = HearthBoard.position(this)
        level().sendParticles(
            ParticleTypes.SMOKE,
            at.x,
            at.y + 0.05,
            at.z,
            0,
            0.0,
            1.0,
            0.0,
            SMOKE_SPEED,
        )
    }

    private fun conditions(player: ServerPlayer): DrillingConditions = DrillingConditions(
        ambient = (PlayerConditions.get(player) ?: PlayerConditions.EMPTY).ambient,
        drillingRise = DRILLING_RISE,
        halfLife = DRILL_HEAT_HALF_LIFE,
        ignition = CHAR_IGNITION,
    )

    public fun drilling(context: WorkContext): LightingFireState.Drilling {
        val conditions = conditions(context.player)
        val jitter = context.player.random.nextIntBetweenInclusive(
            -HandDrillItem.DUST_JITTER_TICKS,
            HandDrillItem.DUST_JITTER_TICKS,
        )
        return LightingFireState.Drilling(
            dust = Duration.NONE,
            dustRequired = Duration((HandDrillItem.BASE_DUST_TICKS + jitter).toLong()),
            heat = conditions.ambient,
            held = false,
            timestamp = context.now,
        )
    }

    override fun exertion(state: LightingFireState): MET? = when (val phase = state.phase) {
        is LightingFireState.Drilling -> HAND_DRILLING.takeIf { phase.held }
        is LightingFireState.NursingEmbers -> BLOWING.takeIf { phase.held }
    }

    public fun nursingEmbers(context: WorkContext, heat: Heat): LightingFireState.NursingEmbers {
        val settled = context.now + EMBER_GRACE
        return LightingFireState.NursingEmbers(
            heat = heat,
            diesAt = emberConditions(context.player).diesAt(heat, settled),
            blown = Duration.NONE,
            blownRequired = Duration(20),
            breathUntil = context.now,
            nextBreathAt = context.now,
            held = false,
            timestamp = settled,
        )
    }

    private inline fun <reified T : Item> ServerPlayer.handUsing(): InteractionHand? =
        if (isUsingItem && useItem.item is T &&
            getItemInHand(usedItemHand).item is T
        ) {
            return usedItemHand
        } else {
            null
        }

    private inline fun <reified T : Item> ServerPlayer.handWorkingOn(
        board: Vec3,
    ): InteractionHand? {
        val hand = handUsing<T>() ?: return null
        if (isFacing(board)) return hand
        stopUsingItem()
        return null
    }
}
