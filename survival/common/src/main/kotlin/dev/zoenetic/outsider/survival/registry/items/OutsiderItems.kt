package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import net.minecraft.core.Direction
import net.minecraft.world.item.Item

public object OutsiderItems {

    public val CAMPFIRE: Item by Survival.platform.register.blockItem(
        "campfire",
        OutsiderBlocks::CAMPFIRE,
    ) { Item.Properties() }

    public val DEAD_TORCH: Item by Survival.platform.register.blockItem(
        "dead_torch",
        OutsiderBlocks::DEAD_TORCH,
    ) { Item.Properties() }

    public val FIREWOOD: Item by Survival.platform.register.blockItem(
        "firewood",
        OutsiderBlocks::FIREWOOD,
    ) { Item.Properties() }

    public val TORCH: Item by Survival.platform.register.standingAndWallBlockItem(
        "torch",
        OutsiderBlocks::TORCH,
        OutsiderBlocks::WALL_TORCH,
        Direction.DOWN,
    ) { Item.Properties() }

    public fun init() {
        OutsiderLooseStoneItems.init()
    }
}
