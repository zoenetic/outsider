package dev.zoenetic.outsider.survival.tools

import net.minecraft.world.item.ItemStack

public interface WorkingToolItem {
    public companion object {
        public fun ItemStack.asWorkingToolItemOrNull(): WorkingToolItem? =
            this.item as? WorkingToolItem
    }
}
