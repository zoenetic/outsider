package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.platform.getValue
import net.minecraft.core.Direction
import net.minecraft.world.item.Item

public object UnbiddenItems {

    public val CAMPFIRE: Item by Survival.platform.register.blockItem(
        "campfire",
        Item.Properties(),
        UnbiddenBlocks::CAMPFIRE,
    )
    
    public val FIREWOOD: Item by Survival.platform.register.blockItem(
        "firewood",
        Item.Properties(),
        UnbiddenBlocks::FIREWOOD,
    )

    public val TORCH: Item by Survival.platform.register.standingAndWallBlockItem(
        "torch",
        UnbiddenBlocks::TORCH,
        UnbiddenBlocks::WALL_TORCH,
        Direction.DOWN,
        Item.Properties()
    )

    public fun init() {}
}