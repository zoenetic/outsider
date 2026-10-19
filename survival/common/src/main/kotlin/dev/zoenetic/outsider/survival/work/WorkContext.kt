package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

public data class WorkContext(val player: ServerPlayer, val now: Time) {
    public val level: ServerLevel
        get() = player.level()

    public companion object {
        public fun of(player: ServerPlayer): WorkContext = WorkContext(
            player = player,
            now = Time(player.level().gameTime),
        )
    }
}
