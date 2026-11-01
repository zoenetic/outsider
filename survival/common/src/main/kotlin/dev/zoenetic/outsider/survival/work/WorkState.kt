package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.vitals.MET
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.phys.Vec3

public interface WorkState {
    public val work: Work<*>

    public fun anchor(): Vec3?
    public fun exertion(): MET?
    public fun tick(context: WorkContext): WorkState?
    public fun tickEffects(context: WorkEffectsContext)

    public companion object {
        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, WorkState> =
            OutsiderWork.STREAM_CODEC.dispatch(WorkState::work) { it.stateCodec }
    }
}
