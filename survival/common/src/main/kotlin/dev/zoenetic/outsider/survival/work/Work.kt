package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

public object Work {
    public fun start(player: ServerPlayer, state: WorkState) {
        if (get(player)?.isSameWorkAs(state) == true) return
        Survival.platform.workState.set(player, state)
    }

    public fun end(player: ServerPlayer) {
        Survival.platform.workState.remove(player)
    }

    public fun tick(level: Level) {
        level.players().forEach { tick(level, it) }
    }

    public fun tick(level: Level, player: Player) {
        val now = Time(level.gameTime)
        val stored = get(player) ?: return
        val effectsContext = WorkEffectsContext(level, player, now)
        stored.tickEffects(effectsContext)
        if (level !is ServerLevel || player !is ServerPlayer) return
        val context = WorkContext.of(level, player)
        val next = stored.tick(context) ?: return end(player)
        if (next != stored) set(player, next)
    }

    public fun get(player: Player): WorkState? = Survival.platform.workState.get(player)

    private fun set(player: Player, state: WorkState): Unit =
        Survival.platform.workState.set(player, state)
}
