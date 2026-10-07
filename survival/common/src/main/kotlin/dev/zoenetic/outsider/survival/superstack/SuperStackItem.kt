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
        if (!accepts(other)) return false
        val handled = when (clickAction) {
            ClickAction.PRIMARY if !other.isEmpty -> {
                if (slot.allowModification(player)) {
                    val _ = superStack.insert(other)
                }
                true
            }

            ClickAction.SECONDARY if other.isEmpty -> {
                if (slot.allowModification(player)) {
                    val _ = carriedItem.set(superStack.splitToNew((superStack.count + 1) / 2))
                }
                true
            }

            else -> false
        }
        if (!handled) {
            toggleSelectedItem(self, -1)
            return false
        }
        superStack.discardIfEmpty()
        player.broadcastInventoryChange()
        return true
    }

    override fun overrideStackedOnOther(
        self: ItemStack,
        slot: Slot,
        clickAction: ClickAction,
        player: Player,
    ): Boolean {
        val superStack = self.asSuperStackOrNull() ?: return false
        val other = slot.item
        val target = other.takeIf { it.`is`(this) }?.asSuperStackOrNull()
        val handled = when {
            target != null -> {
                if (slot.allowModification(player)) {
                    if (clickAction == ClickAction.PRIMARY) {
                        superStack.mergeInto(target)
                    } else {
                        superStack.placeOneInto(target)
                    }
                }
                true
            }

            clickAction == ClickAction.PRIMARY && other.`is`(type.item) -> {
                superStack.takeFrom(slot, player)
                true
            }

            clickAction == ClickAction.SECONDARY && other.isEmpty -> {
                superStack.placeOneInto(slot)
                true
            }

            else -> false
        }
        if (!handled) return false
        superStack.discardIfEmpty()
        player.broadcastInventoryChange()
        return true
    }

    private fun Player.broadcastInventoryChange() {
        containerMenu.slotsChanged(inventory)
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
            val _ = if (result.consumesAction()) {
                superStack.insert(
                    item,
                )
            } else {
                superStack.reinsert(item)
            }
        }
        superStack.discardIfEmpty()
        return result
    }
}
