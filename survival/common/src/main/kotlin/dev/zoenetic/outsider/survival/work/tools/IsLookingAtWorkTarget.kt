package dev.zoenetic.outsider.survival.work.tools

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

public fun Player.isLookingAtWorkTarget(item: Item, blockPos: BlockPos): Boolean {
    val blockState = level().getBlockState(blockPos)
    return (item is WorkingToolItem && item.isValidTarget(blockPos, blockState))
}
