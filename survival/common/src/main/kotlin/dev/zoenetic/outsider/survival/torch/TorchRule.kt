package dev.zoenetic.outsider.survival.torch

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import net.minecraft.world.item.ItemInstance

public val torchRule: Comparator<ItemInstance> = compareByDescending {
    it.getOrDefault(
        OutsiderComponents.FUEL_LEVEL,
        Fuel.MAX
    ).level
}