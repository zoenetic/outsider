package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderLooseStoneBlocks
import net.minecraft.world.item.Item

public object OutsiderLooseStoneItems {
    public val ANDESITE: Item by Survival.platform.register.blockItem(
        "loose_andesite",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val BASALT: Item by Survival.platform.register.blockItem(
        "loose_basalt",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val BLACKSTONE: Item by Survival.platform.register.blockItem(
        "loose_blackstone",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val CALCITE: Item by Survival.platform.register.blockItem(
        "loose_calcite",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val DEEPSLATE: Item by Survival.platform.register.blockItem(
        "loose_deepslate",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val DIORITE: Item by Survival.platform.register.blockItem(
        "loose_diorite",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val ENDSTONE: Item by Survival.platform.register.blockItem(
        "loose_endstone",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val GRANITE: Item by Survival.platform.register.blockItem(
        "loose_granite",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val RED_SANDSTONE: Item by Survival.platform.register.blockItem(
        "loose_red_sandstone",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val SANDSTONE: Item by Survival.platform.register.blockItem(
        "loose_sandstone",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val STONE: Item by Survival.platform.register.blockItem(
        "loose_stone",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public val TUFF: Item by Survival.platform.register.blockItem(
        "loose_tuff",
        OutsiderLooseStoneBlocks::STONE,
    ) { OutsiderItemProperties.LOOSE_STONE }

    public fun init() {}
}
