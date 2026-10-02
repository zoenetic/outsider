package dev.zoenetic.outsider.survival.torch

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import net.minecraft.world.item.ItemInstance

// from least to most fuel, so used torches are used up first

public val torchRule: Comparator<ItemInstance> = compareBy {
    it.getOrDefault(
        OutsiderComponents.FUEL_LEVEL,
        Fuel.MAX
    ).level
}