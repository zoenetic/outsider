package dev.zoenetic.outsider.survival.work

// public fun Player.validWorkingBlockTargetOrNull(tool: WorkingToolItem): WorkingBlockTarget? {
//    val hit = ProjectileUtil.getHitResultOnViewVector(
//        this,
//        EntitySelector.CAN_BE_PICKED,
//        blockInteractionRange(),
//    ) as? BlockHitResult ?: return null
//    if (hit.type != HitResult.Type.BLOCK) return null
//    val blockState = level().getBlockState(hit.blockPos)
//    if (!tool.isValidTarget(hit.blockPos, blockState)) return null
//    return WorkingBlockTarget(hit.blockPos, blockState)
// }
//
// public fun Player.validWorkingBlockTargetOrNull(): WorkingBlockTarget? {
//    val tool =
//        mainHandItem.asWorkingToolItemOrNull() ?: offhandItem.asWorkingToolItemOrNull()
//            ?: return null
//    return validWorkingBlockTargetOrNull(tool)
// }
