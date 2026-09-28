package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.units.Insulation
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.equipment.EquipmentAssets

internal const val LEATHER_WARMTH: Double = 1.0

internal const val HEAD_COVERAGE: Double = 0.15
internal const val CHEST_COVERAGE: Double = 0.40
internal const val LEGS_COVERAGE: Double = 0.30
internal const val FEET_COVERAGE: Double = 0.15

public fun Player.getInsulation(): Insulation {
    var added = 0.0
    for (slot in EquipmentSlotGroup.ARMOR) {
        val stack = getItemBySlot(slot)
        val material = stack.get(DataComponents.EQUIPPABLE)?.assetId?.orElse(null)
        val warmth = when (material) {
            EquipmentAssets.LEATHER -> LEATHER_WARMTH
            else -> 0.0
        }
        val coverage = when (slot) {
            EquipmentSlot.HEAD -> HEAD_COVERAGE
            EquipmentSlot.CHEST -> CHEST_COVERAGE
            EquipmentSlot.LEGS -> LEGS_COVERAGE
            EquipmentSlot.FEET -> FEET_COVERAGE
            else -> 0.0
        }
        added += warmth * coverage
    }
    return Insulation(1.0 + added)
}
