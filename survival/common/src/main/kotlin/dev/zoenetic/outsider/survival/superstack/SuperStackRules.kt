package dev.zoenetic.outsider.survival.superstack

import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.item.ItemInstance
import net.minecraft.world.item.ItemStackTemplate

public interface SuperStackRules {
    public fun onEnter(entering: ItemStackTemplate): DataComponentPatch = DataComponentPatch.EMPTY
    public val sort: Comparator<ItemInstance>?
        get() = null

    public companion object {
        public val NONE: SuperStackRules = object : SuperStackRules {}
    }
}
