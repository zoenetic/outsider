package dev.zoenetic.outsider.survival.tools

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

public class TinderItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = MAX_BLOW_TICKS

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val hand = context.hand
        val blockPos = context.clickedPos
        return OutsiderWork.LIGHTING_FIRE.use(player, hand, this, blockPos)
    }

    public companion object {
        public const val MAX_BLOW_TICKS: Int = 100
    }
}
