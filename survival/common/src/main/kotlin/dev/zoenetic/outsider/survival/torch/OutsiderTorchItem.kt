package dev.zoenetic.outsider.survival.torch

import dev.zoenetic.outsider.survival.superstack.SuperStackHooks
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.StandingAndWallBlockItem
import net.minecraft.world.level.block.Block

public class OutsiderTorchItem(
    block: Block,
    wallBlock: Block,
    attachmentDirection: Direction,
    properties: Properties,
) : StandingAndWallBlockItem(block, wallBlock, attachmentDirection, properties) {

    override fun inventoryTick(
        itemStack: ItemStack,
        level: ServerLevel,
        owner: Entity,
        slot: EquipmentSlot?,
    ) {
        SuperStackHooks.routeInventoryTick(itemStack, owner)
    }
}
