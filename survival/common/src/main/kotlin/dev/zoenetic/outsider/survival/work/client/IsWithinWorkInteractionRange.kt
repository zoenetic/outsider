package dev.zoenetic.outsider.survival.work.client

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB

private const val WORK_INTERACTION_RANGE: Double = 1.5

public fun Player.isWithinWorkInteractionRange(blockPos: BlockPos): Boolean =
    AABB(blockPos).distanceToSqr(eyePosition) < WORK_INTERACTION_RANGE
