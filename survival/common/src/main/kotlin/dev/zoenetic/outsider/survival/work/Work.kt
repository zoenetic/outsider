package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level

public abstract class Work<S : WorkState>(public val id: Identifier) {

    public abstract val stateCodec: StreamCodec<RegistryFriendlyByteBuf, S>

    public abstract fun tick(context: WorkContext, state: S): S?
    public open fun tickEffects(context: WorkEffectsContext, state: S) {}

    public abstract fun use(
        player: Player,
        hand: InteractionHand,
        item: Item,
        blockPos: BlockPos,
    ): InteractionResult

    protected fun start(player: ServerPlayer, state: S): S? {
        val stored = get(player)
        if (stored != null && stored.work !== this) return null
        set(player, state)
        return state
    }

    public companion object {
        public fun get(player: Player): WorkState? = Survival.platform.workState.get(player)
        public fun end(player: ServerPlayer): Unit = Survival.platform.workState.remove(player)
        private fun set(player: ServerPlayer, state: WorkState) =
            Survival.platform.workState.set(player, state)

        public fun tick(level: Level) {
            level.players().forEach { tick(it, level) }
        }

        public fun tick(player: Player, level: Level) {
            val now = Time(level.gameTime)
            val stored = get(player) ?: return
            val effectsContext = WorkEffectsContext(level, player, now)
            stored.tickEffects(effectsContext)
            if (level !is ServerLevel || player !is ServerPlayer) return
            val context = WorkContext.of(player)
            val next = stored.tick(context) ?: return end(player)
            set(player, next)
        }
    }
}
