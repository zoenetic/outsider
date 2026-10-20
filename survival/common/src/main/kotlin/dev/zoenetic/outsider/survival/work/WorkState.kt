package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

public interface WorkState {
    public val work: Work<*>

    public fun tick(context: WorkContext): WorkState?
    public fun tickEffects(context: WorkEffectsContext)

    public companion object {
        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, WorkState> =
            OutsiderWork.STREAM_CODEC.dispatch(WorkState::work) { it.stateCodec }
    }
}
