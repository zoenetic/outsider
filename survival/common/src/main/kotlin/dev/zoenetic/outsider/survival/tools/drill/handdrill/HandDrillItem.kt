package dev.zoenetic.outsider.survival.tools.drill.handdrill

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.tools.WorkingToolItem
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

public class HandDrillItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = MAX_DRILL_TICKS

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val hand = context.hand
        val blockPos = context.clickedPos
        return OutsiderWork.LIGHTING_FIRE.use(player, hand, this, blockPos)
    }

    public companion object {
        public const val BASE_DUST_TICKS: Int = 160
        public const val DUST_JITTER_TICKS: Int = 60
        public const val MAX_DRILL_TICKS: Int = 600
        public const val TICKS_PER_WEAR: Long = 20L
        public const val USES: Int = 120
    }
}
