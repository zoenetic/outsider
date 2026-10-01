package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.platform.getValue
import net.minecraft.core.Direction
import net.minecraft.world.item.Item

public object OutsiderItems {

    public val CAMPFIRE: Item by Survival.platform.register.blockItem(
        "campfire",
        Item.Properties(),
        OutsiderBlocks::CAMPFIRE,
    )

    public val FIREWOOD: Item by Survival.platform.register.blockItem(
        "firewood",
        Item.Properties(),
        OutsiderBlocks::FIREWOOD,
    )

    public val TORCH: Item by Survival.platform.register.standingAndWallBlockItem(
        "torch",
        OutsiderBlocks::TORCH,
        OutsiderBlocks::WALL_TORCH,
        Direction.DOWN,
        Item.Properties()
            .component(OutsiderComponents.FUEL_LEVEL, Fuel.MAX)
    )

    public fun init() {}
}