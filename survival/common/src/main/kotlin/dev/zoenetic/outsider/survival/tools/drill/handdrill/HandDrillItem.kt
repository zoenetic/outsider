package dev.zoenetic.outsider.survival.tools.drill.handdrill

import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.tools.TinderItem.Companion.holdsLitTinder
import dev.zoenetic.outsider.survival.tools.WorkingToolItem
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

public class HandDrillItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = 72000

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (player.holdsLitTinder()) return InteractionResult.PASS
        return OutsiderWork.LIGHTING_FIRE.use(player, hand, this)
    }

    public companion object {
        public const val BASE_DUST_TICKS: Int = 160
        public const val DUST_JITTER_TICKS: Int = 60
        public const val TICKS_PER_WEAR: Long = 20L
        public const val USES: Int = 120
    }
}
