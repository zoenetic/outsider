package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.tools.drill.handdrill.DrillingConditions
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.MET
import dev.zoenetic.outsider.survival.work.WorkContext
import dev.zoenetic.outsider.survival.work.WorkEffectsContext
import dev.zoenetic.outsider.survival.work.WorkState
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3

public data class LightingFireState(val board: Vec3, val phase: Phase) : WorkState {

    override val work: LightingFire get() = OutsiderWork.LIGHTING_FIRE

    override fun anchor(): Vec3 = work.anchor(this)

    override fun exertion(): MET? = work.exertion(this)

    override fun tick(context: WorkContext): LightingFireState? = work.tick(context, this)

    override fun tickEffects(context: WorkEffectsContext): Unit = work.tickEffects(context, this)

    public sealed interface Phase {
        public val kind: Kind

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
        val dust: Duration,
        val dustRequired: Duration,
        val heat: Heat,
        val held: Boolean,
        val timestamp: Time,
    ) : Phase {
        override val kind: Phase.Kind get() = Phase.Kind.DRILLING

        public fun dustAt(now: Time): Duration {
            if (held) {
                return dust + (now - timestamp)
            }
            return dust
        }

        public fun heatAt(now: Time, conditions: DrillingConditions): Heat = with(conditions) {
            heat.approach(
                if (held) ambient + drillingRise else ambient,
                now - timestamp,
                halfLife,
            )
        }

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Drilling> =
                StreamCodec.composite(
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
        val heat: Heat,
        val diesAt: Time,
        val blown: Duration,
        val blownRequired: Duration,
        val breathUntil: Time,
        val nextBreathAt: Time,
        val held: Boolean,
        val timestamp: Time,
    ) : Phase {
        override val kind: Phase.Kind get() = Phase.Kind.NURSING_EMBERS

        public fun blownAt(now: Time): Duration {
            if (held) {
                return blown + (now - timestamp)
            }
            return blown
        }

        public fun heatAt(now: Time, conditions: EmberConditions): Heat = with(conditions) {
            heat.approach(
                if (held) ambient + blowingRise else ambient,
                now - timestamp,
                halfLife,
            )
        }

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, NursingEmbers> =
                StreamCodec.composite(
                    Heat.STREAM_CODEC,
                    NursingEmbers::heat,
                    Time.STREAM_CODEC,
                    NursingEmbers::diesAt,
                    Duration.STREAM_CODEC,
                    NursingEmbers::blown,
                    Duration.STREAM_CODEC,
                    NursingEmbers::blownRequired,
                    Time.STREAM_CODEC,
                    NursingEmbers::breathUntil,
                    Time.STREAM_CODEC,
                    NursingEmbers::nextBreathAt,
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
                Vec3.STREAM_CODEC,
                LightingFireState::board,
                Phase.STREAM_CODEC,
                LightingFireState::phase,
                ::LightingFireState,
            )
    }
}
