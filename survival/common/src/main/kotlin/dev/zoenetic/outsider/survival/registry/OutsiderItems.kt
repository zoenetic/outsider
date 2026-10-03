package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.superstack.SuperStackItem
import dev.zoenetic.outsider.survival.superstack.SuperStackType
import dev.zoenetic.outsider.survival.torch.TorchRules
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS
import net.minecraft.world.item.Item
import net.minecraft.world.item.component.BundleContents

public object OutsiderItems {

    public val CAMPFIRE: Item by Survival.platform.register.blockItem(
        "campfire",
        OutsiderBlocks::CAMPFIRE,
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
    ) {
        Item.Properties()
            .component(OutsiderComponents.FUEL_LEVEL, Fuel.MAX)
    }

    public val TORCH_SUPERSTACK: Item by Survival.platform.register.item(
        "torch_superstack",
        { properties ->
            SuperStackItem(
                SuperStackType(
                    TORCH,
                    TorchRules,
                    listOf(OutsiderComponents.FUEL_LEVEL, OutsiderComponents.LIT),
                ),
                properties,
            )
        },
        {
            Item.Properties()
                .component(BUNDLE_CONTENTS, BundleContents.EMPTY)
                .stacksTo(1)
        },
    )

    public fun init() {}
}
