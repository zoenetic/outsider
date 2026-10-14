package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem
import dev.zoenetic.outsider.survival.work.tools.WorkingToolItem.Companion.asWorkingToolItemOrNull
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

public fun Player.validWorkingBlockTargetOrNull(tool: WorkingToolItem): WorkingBlockTarget? {
    val hit = ProjectileUtil.getHitResultOnViewVector(
        this,
        EntitySelector.CAN_BE_PICKED,
        blockInteractionRange(),
    ) as? BlockHitResult ?: return null
    if (hit.type != HitResult.Type.BLOCK) return null
    val blockState = level().getBlockState(hit.blockPos)
    if (!tool.isValidTarget(hit.blockPos, blockState)) return null
    return WorkingBlockTarget(hit.blockPos, blockState)
}

public fun Player.validWorkingBlockTargetOrNull(): WorkingBlockTarget? {
    val tool =
        mainHandItem.asWorkingToolItemOrNull() ?: offhandItem.asWorkingToolItemOrNull()
            ?: return null
    return validWorkingBlockTargetOrNull(tool)
}
