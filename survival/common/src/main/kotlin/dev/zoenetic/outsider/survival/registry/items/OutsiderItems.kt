package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.tools.TinderItem
import dev.zoenetic.outsider.survival.tools.drill.handdrill.HandDrillItem
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.UseEffects

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

    public val HAND_DRILL: Item by Survival.platform.register.item(
        "hand_drill",
        ::HandDrillItem,
    ) {
        Item.Properties()
            .durability(HandDrillItem.USES)
            .component(DataComponents.USE_EFFECTS, UseEffects(false, true, 0F))
    }

    public val TINDER: Item by Survival.platform.register.item(
        "tinder",
        ::TinderItem,
    ) {
        Item.Properties()
            .durability(1)
    }

    public val TORCH: Item by Survival.platform.register.standingAndWallBlockItem(
        "torch",
        OutsiderBlocks::TORCH,
        OutsiderBlocks::WALL_TORCH,
        Direction.DOWN,
    ) { Item.Properties() }

    public fun init(): Unit = Unit
}
