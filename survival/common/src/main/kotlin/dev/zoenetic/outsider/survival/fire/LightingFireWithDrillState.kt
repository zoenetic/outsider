package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.WorkState
import dev.zoenetic.outsider.survival.work.WorkState.Kind
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionHand

public data class LightingFireWithDrillState(
    override val blockPos: BlockPos,
    override val hand: InteractionHand,
    override val phase: Phase,
) : WorkState {

    override val kind: Kind
        get() = Kind.LIGHTING_FIRE_WITH_DRILL

    public sealed interface Phase : WorkState.Phase {
        public val kind: Kind

        public enum class Kind(
            public val codec: () -> StreamCodec<RegistryFriendlyByteBuf, out Phase>,
        ) {
            DRILLING({ Drilling.STREAM_CODEC }),
            BLOWING_EMBERS({ BlowingEmbers.STREAM_CODEC }),
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

    public data class Drilling(public val elapsed: Duration, public val required: Duration) :
        Phase {
        override val kind: Phase.Kind = Phase.Kind.DRILLING

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Drilling> =
                StreamCodec.composite(
                    Duration.STREAM_CODEC,
                    Drilling::elapsed,
                    Duration.STREAM_CODEC,
                    Drilling::required,
                    ::Drilling,
                )
        }
    }

    public data class BlowingEmbers(val diesAt: Time, public val required: Duration) : Phase {
        override val kind: Phase.Kind = Phase.Kind.BLOWING_EMBERS

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, BlowingEmbers> =
                StreamCodec.composite(
                    Time.STREAM_CODEC,
                    BlowingEmbers::diesAt,
                    Duration.STREAM_CODEC,
                    BlowingEmbers::required,
                    ::BlowingEmbers,
                )
        }
    }

    public companion object {
        public fun new(
            blockPos: BlockPos,
            hand: InteractionHand,
            drillingTimeRequired: Duration,
        ): LightingFireWithDrillState = LightingFireWithDrillState(
            blockPos,
            hand,
            Drilling(Duration.NONE, drillingTimeRequired),
        )

        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LightingFireWithDrillState> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                LightingFireWithDrillState::blockPos,
                InteractionHand.STREAM_CODEC,
                LightingFireWithDrillState::hand,
                Phase.STREAM_CODEC,
                LightingFireWithDrillState::phase,
                ::LightingFireWithDrillState,
            )
    }
}
