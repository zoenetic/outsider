package dev.zoenetic.outsider.survival.superstack

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.BundleItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult

public class SuperStackItem(public val type: SuperStackType, properties: Properties) :
    BundleItem(properties) {

    private fun accepts(incoming: ItemStack): Boolean = incoming.isEmpty || incoming.`is`(type.item)

    override fun inventoryTick(
        itemStack: ItemStack,
        level: ServerLevel,
        owner: Entity,
        slot: EquipmentSlot?,
    ) {
        val superStack = itemStack.asSuperStackOrNull() ?: return
        superStack.refreshMirroredComponents()
        superStack.discardIfEmpty()
    }

    override fun isBarVisible(stack: ItemStack): Boolean = false

    override fun overrideOtherStackedOnMe(
        self: ItemStack,
        other: ItemStack,
        slot: Slot,
        clickAction: ClickAction,
        player: Player,
        carriedItem: SlotAccess,
    ): Boolean {
        val superStack = self.asSuperStackOrNull() ?: return false
        if (accepts(other)) {
            val result = super.overrideOtherStackedOnMe(
                self,
                other,
                slot,
                clickAction,
                player,
                carriedItem,
            )
            superStack.discardIfEmpty()
            return result
        }
        return false
    }

    override fun overrideStackedOnOther(
        self: ItemStack,
        slot: Slot,
        clickAction: ClickAction,
        player: Player,
    ): Boolean {
        val superStack = self.asSuperStackOrNull() ?: return false
        if (accepts(slot.item)) {
            val result = super.overrideStackedOnOther(
                self,
                slot,
                clickAction,
                player,
            )
            superStack.discardIfEmpty()
            return result
        }
        return false
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
        InteractionResult.PASS

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
        superStack.discardIfEmpty()
        return result
    }
}
