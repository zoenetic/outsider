package dev.zoenetic.outsider.survival.registry.blocks

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock
import net.minecraft.world.level.block.Block

public object OutsiderLooseStoneBlocks {

    public val ANDESITE: Block by Survival.platform.register.block(
        "loose_andesite",
        { props -> LooseStoneBlock(LooseStoneArrangements.ANDESITE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val BASALT: Block by Survival.platform.register.block(
        "loose_basalt",
        { props -> LooseStoneBlock(LooseStoneArrangements.BASALT, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val BLACKSTONE: Block by Survival.platform.register.block(
        "loose_blackstone",
        { props -> LooseStoneBlock(LooseStoneArrangements.BLACKSTONE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val CALCITE: Block by Survival.platform.register.block(
        "loose_calcite",
        { props -> LooseStoneBlock(LooseStoneArrangements.CALCITE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val DEEPSLATE: Block by Survival.platform.register.block(
        "loose_deepslate",
        { props -> LooseStoneBlock(LooseStoneArrangements.DEEPSLATE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val DIORITE: Block by Survival.platform.register.block(
        "loose_diorite",
        { props -> LooseStoneBlock(LooseStoneArrangements.DIORITE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val ENDSTONE: Block by Survival.platform.register.block(
        "loose_endstone",
        { props -> LooseStoneBlock(LooseStoneArrangements.ENDSTONE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val GRANITE: Block by Survival.platform.register.block(
        "loose_granite",
        { props -> LooseStoneBlock(LooseStoneArrangements.GRANITE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val RED_SANDSTONE: Block by Survival.platform.register.block(
        "loose_red_sandstone",
        { props -> LooseStoneBlock(LooseStoneArrangements.RED_SANDSTONE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val SANDSTONE: Block by Survival.platform.register.block(
        "loose_sandstone",
        { props -> LooseStoneBlock(LooseStoneArrangements.SANDSTONE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val STONE: Block by Survival.platform.register.block(
        "loose_stone",
        { props -> LooseStoneBlock(LooseStoneArrangements.STONE, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public val TUFF: Block by Survival.platform.register.block(
        "loose_tuff",
        { props -> LooseStoneBlock(LooseStoneArrangements.TUFF, props) },
        { OutsiderBlockBehaviour.Properties.looseStone() },
    )

    public fun init() {}
}
