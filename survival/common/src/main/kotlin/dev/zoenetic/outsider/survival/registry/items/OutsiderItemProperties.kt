package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock
import net.minecraft.world.item.Item

public object OutsiderItemProperties {

    public val LOOSE_STONE: Item.Properties = Item.Properties().stacksTo(LooseStoneBlock.STACK_SIZE)
}
