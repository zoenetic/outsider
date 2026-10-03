package dev.zoenetic.outsider.survival.superstack

import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BundleItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult

public class SuperStackItem(public val type: SuperStackType, properties: Properties) :
    BundleItem(properties) {

    override fun useOn(context: UseOnContext): InteractionResult {
        val stack = context.itemInHand
        val superStack = stack.asSuperStackOrNull() ?: return InteractionResult.PASS
        val player = context.player ?: return InteractionResult.PASS
        val hand = context.hand
        val hitResult = BlockHitResult(
            context.clickLocation,
            context.clickedFace,
            context.clickedPos,
            context.isInside,
        )
        val item = superStack.split(1)
        val newContext = BlockPlaceContext(player, hand, item, hitResult)
        val result = item.useOn(newContext)
        if (!item.isEmpty) {
            val _ = superStack.insert(item)
        }
        return result
    }
}
