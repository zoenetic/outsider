package dev.zoenetic.outsider.survival.work.client

import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player

public fun workingStrength(entity: LivingEntity): Float =
    if (entity is Player && Work.get(entity) != null) 1F else 0F
