package dev.zoenetic.outsider.survival.work

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item

public fun Player.isUsingWorkItem(item: Item, hand: InteractionHand): Boolean =
    isUsingItem && usedItemHand == hand && getItemInHand(hand).item == item
