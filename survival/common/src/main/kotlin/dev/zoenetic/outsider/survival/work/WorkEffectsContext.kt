package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

public data class WorkEffectsContext(val level: Level, val player: Player, val now: Time)
