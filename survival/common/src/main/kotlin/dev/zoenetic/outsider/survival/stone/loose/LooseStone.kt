package dev.zoenetic.outsider.survival.stone.loose

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockBehaviour
import dev.zoenetic.outsider.survival.registry.items.OutsiderItemProperties
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

public class LooseStone(
    public val name: String,
    public val arrangement: LooseStoneArrangement,
    public val texture: Identifier,
) {
    public val block: Block by Survival.platform.register.block(
        "loose_$name",
        { props -> LooseStoneBlock(arrangement, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )
    public val item: Item by Survival.platform.register.blockItem("loose_$name", ::block) {
        OutsiderItemProperties.LOOSE_STONE
    }
}
