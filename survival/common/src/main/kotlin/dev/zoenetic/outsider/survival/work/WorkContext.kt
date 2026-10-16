package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

public data class WorkContext(val level: ServerLevel, val player: ServerPlayer, val now: Time) {
    public companion object {
        public fun of(level: ServerLevel, player: ServerPlayer): WorkContext = WorkContext(
            level,
            player,
            Time(level.gameTime),
        )
    }
}
