package dev.zoenetic.outsider.survival.work.tools.drill.handdrill

import dev.zoenetic.outsider.survival.fire.TinderItem
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.WorkContext
import dev.zoenetic.outsider.survival.work.WorkEffectsContext
import dev.zoenetic.outsider.survival.work.WorkState
import dev.zoenetic.outsider.survival.work.isUsingWorkItem
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFire.isLightable
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import kotlin.math.pow

public data class LightingFireState(override val blockPos: BlockPos, override val phase: Phase) :
    WorkState {
    override val kind: WorkState.Kind
        get() = WorkState.Kind.LIGHTING_FIRE_WITH_DRILL

    override fun tick(context: WorkContext): LightingFireState? = phase.tick(context, this)?.let {
        copy(phase = it)
    }

    override fun tickEffects(context: WorkEffectsContext) {
        phase.tickEffects(context, blockPos)
    }

    public sealed interface Phase : WorkState.Phase {
        public val kind: Kind

        override fun tick(context: WorkContext, work: WorkState): Phase?
        override fun tickEffects(context: WorkEffectsContext, blockPos: BlockPos)

        public enum class Kind(
            public val codec: () -> StreamCodec<RegistryFriendlyByteBuf, out Phase>,
        ) {
            DRILLING({ Drilling.STREAM_CODEC }),
            NURSING_EMBERS({ NursingEmbers.STREAM_CODEC }),
            ;

            public companion object {
                public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Kind> =
                    ByteBufCodecs.idMapper({ entries[it] }, Kind::ordinal).cast()
            }
        }

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Phase> =
                Kind.STREAM_CODEC.dispatch(
                    Phase::kind,
                ) {
                    it.codec()
                }
        }
    }

    public data class Drilling(
        val hand: InteractionHand,
        val dust: Duration,
        val dustRequired: Duration,
        val heat: Heat,
        val held: Boolean,
        val timestamp: Time,
    ) : Phase {
        override val kind: Phase.Kind = Phase.Kind.DRILLING

        public fun dustAt(now: Time): Duration {
            if (held) {
                return dust + (now - timestamp)
            }
            return dust
        }

        public fun heatAt(now: Time, conditions: DrillingConditions): Heat = with(conditions) {
            if (held) {
                val elapsed = now - timestamp
                return (heat + risePerTick * elapsed.value.toDouble()).coerceAtMost(ignition)
            } else {
                val elapsed = (now - timestamp).value.toDouble()
                return ambient + (heat - ambient) * 0.5.pow(elapsed / halfLife.value)
            }
        }

        override fun tick(context: WorkContext, work: WorkState): Phase? {
            val level = context.level
            val player = context.player
            val now = context.now
            val drill = OutsiderItems.HAND_DRILL
            if (player.getItemInHand(hand).item != drill || !level.isLightable(work.blockPos)) {
                return null
            }
            val conditions = LightingFire.conditions(level, work.blockPos)
            val dust = dustAt(now)
            val heat = heatAt(now, conditions)
            val using = player.isUsingWorkItem(drill, hand)
            if (using != held) {
                return copy(dust = dust, heat = heat, held = using, timestamp = now)
            }
            if (!held) return this

            if (dust > Duration.NONE && dust.value % HandDrillItem.TICKS_PER_WEAR == 0L) {
                player.getItemInHand(hand).hurtAndBreak(1, player, hand)
            }
            if (dust >= dustRequired && heat >= conditions.ignition) {
                player.stopUsingItem()
                return LightingFire.nursingEmbers(context)
            }
            return this
        }

        override fun tickEffects(context: WorkEffectsContext, blockPos: BlockPos) {
            if (dustAt(context.now).value % 7L == 0L) {
                context.level.playSound(
                    context.player,
                    blockPos,
                    OutsiderSounds.USE_DRILL_SOUND,
                    SoundSource.BLOCKS,
                )
            }
        }

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Drilling> =
                StreamCodec.composite(
                    InteractionHand.STREAM_CODEC,
                    Drilling::hand,
                    Duration.STREAM_CODEC,
                    Drilling::dust,
                    Duration.STREAM_CODEC,
                    Drilling::dustRequired,
                    Heat.STREAM_CODEC,
                    Drilling::heat,
                    ByteBufCodecs.BOOL,
                    Drilling::held,
                    Time.STREAM_CODEC,
                    Drilling::timestamp,
                    ::Drilling,
                )
        }
    }

    public data class NursingEmbers(
        val diesAt: Time,
        val blown: Duration,
        val blownRequired: Duration,
        val held: Boolean,
        val timestamp: Time,
    ) : Phase {
        override val kind: Phase.Kind = Phase.Kind.NURSING_EMBERS

        public fun blownAt(now: Time): Duration = blown + (now - timestamp)

        override fun tick(context: WorkContext, work: WorkState): Phase? {
            val player = context.player
            val hand = player.usedItemHand
            if (context.now >= diesAt) {
                player.stopUsingItem()
                return LightingFire.drilling(hand, work.blockPos, context)
            }
            if (!held) {
                return copy(
                    diesAt = diesAt,
                    blown = blown,
                    blownRequired = blownRequired,
                    held = false,
                    timestamp = context.now,
                )
            }
            if (blownRequired > Duration.NONE &&
                blownRequired.value % TinderItem.TICKS_PER_SOMETHING == 0L
            ) {
                // TODO: Do something about exertion/breathing/etc
            }
            if (blown >= blownRequired) {
                player.stopUsingItem()
                return null
            }
            return this
        }

        override fun tickEffects(context: WorkEffectsContext, blockPos: BlockPos) {
            if (held && blown >= blownRequired) {
                context.level.playSound(
                    null,
                    blockPos,
                    OutsiderSounds.FIRE_SUCCESS,
                    SoundSource.BLOCKS,
                    1F,
                    1F,
                )
            }
        }

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, NursingEmbers> =
                StreamCodec.composite(
                    Time.STREAM_CODEC,
                    NursingEmbers::diesAt,
                    Duration.STREAM_CODEC,
                    NursingEmbers::blown,
                    Duration.STREAM_CODEC,
                    NursingEmbers::blownRequired,
                    ByteBufCodecs.BOOL,
                    NursingEmbers::held,
                    Time.STREAM_CODEC,
                    NursingEmbers::timestamp,
                    ::NursingEmbers,
                )
        }
    }

    public companion object {
        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LightingFireState> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                LightingFireState::blockPos,
                Phase.STREAM_CODEC,
                LightingFireState::phase,
                ::LightingFireState,
            )
    }
}
