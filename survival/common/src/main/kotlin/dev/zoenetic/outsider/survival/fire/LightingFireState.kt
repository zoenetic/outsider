package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.work.WorkState
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionHand

public data class LightingFireState(
    override val target: BlockPos,
    override val hand: InteractionHand,
    // TODO: elapsed changes every drilling tick, so it syncs every tick; maybe start time + banked
    val elapsed: Int,
    val required: Int,
) : WorkState {
    override val kind: WorkState.Kind get() = WorkState.Kind.LIGHTING_FIRE

    public companion object {
        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LightingFireState> =
            StreamCodec.composite(
                BlockPos.STREAM_CODEC,
                LightingFireState::target,
                InteractionHand.STREAM_CODEC,
                LightingFireState::hand,
                ByteBufCodecs.VAR_INT,
                LightingFireState::elapsed,
                ByteBufCodecs.VAR_INT,
                LightingFireState::required,
                ::LightingFireState,
            )
    }
}
