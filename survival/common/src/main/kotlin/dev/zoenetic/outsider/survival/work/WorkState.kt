package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

public interface WorkState {

    public val kind: Kind
    public val blockPos: BlockPos
    public val phase: Phase

    public fun tick(context: WorkContext): WorkState?
    public fun tickEffects(context: WorkEffectsContext)

    public fun isSameWorkAs(other: WorkState?): Boolean =
        other != null && kind == other.kind && blockPos == other.blockPos

    public interface Phase {
        public fun tick(context: WorkContext, work: WorkState): Phase?
        public fun tickEffects(context: WorkEffectsContext, blockPos: BlockPos)
    }

    public enum class Kind(
        public val codec: () -> StreamCodec<RegistryFriendlyByteBuf, out WorkState>,
    ) {
        LIGHTING_FIRE_WITH_DRILL(
            {
                LightingFireState.STREAM_CODEC
            },
        ),
        ;

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Kind> =
                ByteBufCodecs.idMapper({ entries[it] }, Kind::ordinal).cast()
        }
    }

    public companion object {
        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, WorkState> =
            Kind.STREAM_CODEC.dispatch(
                WorkState::kind,
            ) {
                it.codec()
            }
    }
}
