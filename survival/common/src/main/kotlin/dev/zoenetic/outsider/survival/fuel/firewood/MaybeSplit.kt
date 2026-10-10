package dev.zoenetic.outsider.survival.fuel.firewood

import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block.popResource
import net.minecraft.world.level.block.state.properties.BlockStateProperties

public fun maybeSplit(context: UseOnContext): Boolean {
    val level: Level = context.level
    val pos: BlockPos = context.clickedPos
    val player: Player? = context.player
    player?.let {
        if (!level.isClientSide) {
            val state = level.getBlockState(pos)
            if (state.canBeSplit()) {
                if (context.clickedFace.axis === state.getValue(BlockStateProperties.AXIS)) {
                    level.destroyBlock(pos, false, player, 512)
                    val firewood = ItemStack(OutsiderItems.FIREWOOD, FirewoodBlock.MAX_BILLETS)
                    popResource(level, pos, firewood)
                    val axe = context.itemInHand
                    axe.hurtAndBreak(1, player, context.hand)
                }
            }
            return true
        }
    }
    return false
}
