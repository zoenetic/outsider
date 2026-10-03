package dev.zoenetic.outsider.survival.torch

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.superstack.SuperStackRules
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.ItemStackTemplate

public object TorchRules : SuperStackRules {
    override fun onEnter(entering: ItemStackTemplate): DataComponentPatch =
        DataComponentPatch.builder().remove(OutsiderComponents.LIT).build()

    /** Sort lit torches first, then the rest from least to most fuel, so used torches get priority. */
    override val sort: Comparator<ItemInstance> =
        compareByDescending<ItemInstance> {
            it.get(OutsiderComponents.LIT) != null
        }.thenBy {
            it.getOrDefault(
                OutsiderComponents.FUEL_LEVEL,
                Fuel.MAX,
            ).level
        }
}
