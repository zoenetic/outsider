package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem.Companion.asWorkingToolItemOrNull
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player

public interface WorkState {

    public val kind: Kind
    public val target: BlockPos
    public val hand: InteractionHand

    public enum class Kind(
        public val codec: () -> StreamCodec<RegistryFriendlyByteBuf, out WorkState>,
    ) {
        LIGHTING_FIRE({ LightingFireState.STREAM_CODEC }),
        ;

        public companion object {
            public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Kind> =
                ByteBufCodecs.idMapper({ entries[it] }, Kind::ordinal).cast()
        }
    }

    public companion object {
        public fun enter(player: Player, state: WorkState) {
            val storedState = Survival.platform.workState.get(player)
            if (storedState != null) Survival.platform.workState.remove(player)
            Survival.platform.workState.set(player, state)
        }

        public fun exit(player: Player) {
            Survival.platform.workState.remove(player)
        }

        // TODO: call from a per-player tick on both loaders (server, plus client for prediction)
        public fun tick(player: Player) {
            val storedState = Survival.platform.workState.get(player) ?: return
            val currentTool =
                player.getItemInHand(storedState.hand).asWorkingToolItemOrNull()
                    ?: return exit(player)
            val currentTarget =
                player.workingBlockTargetOrNull(currentTool) ?: return exit(player)
            if (currentTarget.blockPos != storedState.target) return exit(player)
            val newState = currentTool.tickWork(player, storedState) ?: return exit(player)
            if (newState != storedState) Survival.platform.workState.set(player, newState)
        }

        public fun get(player: Player): WorkState? = Survival.platform.workState.get(player)

        public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, WorkState> =
            Kind.STREAM_CODEC.dispatch(
                WorkState::kind,
            ) {
                it.codec()
            }
    }
}
