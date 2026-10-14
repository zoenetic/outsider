package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionHand

public interface WorkState {

    public val kind: Kind
    public val blockPos: BlockPos
    public val hand: InteractionHand
    public val phase: Phase

    public fun isSameWorkAs(other: WorkState): Boolean =
        kind == other.kind && blockPos == other.blockPos && hand == other.hand

    public enum class Kind(
        public val codec: () -> StreamCodec<RegistryFriendlyByteBuf, out WorkState>,
    ) {
        LIGHTING_FIRE_WITH_DRILL({ LightingFireWithDrillState.STREAM_CODEC }),
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

    public interface Phase
}
