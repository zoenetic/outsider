package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.stone.loose.LooseStone
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangement
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements
import net.minecraft.resources.Identifier

public object OutsiderLooseStones {
    public val ALL: List<LooseStone>
        field = mutableListOf<LooseStone>()

    public val ANDESITE: LooseStone =
        stone(
            "andesite",
            LooseStoneArrangements.ANDESITE,
            Identifier.withDefaultNamespace("block/andesite"),
        )

    public val BASALT: LooseStone =
        stone(
            "basalt",
            LooseStoneArrangements.BASALT,
            Identifier.withDefaultNamespace("block/basalt_side"),
        )

    public val BLACKSTONE: LooseStone =
        stone(
            "blackstone",
            LooseStoneArrangements.BLACKSTONE,
            Identifier.withDefaultNamespace("block/blackstone"),
        )
    public val CALCITE: LooseStone =
        stone(
            "calcite",
            LooseStoneArrangements.CALCITE,
            Identifier.withDefaultNamespace("block/calcite"),
        )

    public val DEEPSLATE: LooseStone =
        stone(
            "deepslate",
            LooseStoneArrangements.DEEPSLATE,
            Identifier.withDefaultNamespace("block/deepslate"),
        )

    public val DIORITE: LooseStone =
        stone(
            "diorite",
            LooseStoneArrangements.DIORITE,
            Identifier.withDefaultNamespace("block/diorite"),
        )

    public val END_STONE: LooseStone =
        stone(
            "end_stone",
            LooseStoneArrangements.END_STONE,
            Identifier.withDefaultNamespace("block/end_stone"),
        )

    public val GRANITE: LooseStone =
        stone(
            "granite",
            LooseStoneArrangements.GRANITE,
            Identifier.withDefaultNamespace("block/granite"),
        )

    public val RED_SANDSTONE: LooseStone =
        stone(
            "red_sandstone",
            LooseStoneArrangements.RED_SANDSTONE,
            Identifier.withDefaultNamespace("block/red_sandstone"),
        )

    public val SANDSTONE: LooseStone =
        stone(
            "sandstone",
            LooseStoneArrangements.SANDSTONE,
            Identifier.withDefaultNamespace("block/sandstone"),
        )

    public val STONE: LooseStone =
        stone(
            "stone",
            LooseStoneArrangements.STONE,
            Identifier.withDefaultNamespace("block/stone"),
        )

    public val TUFF: LooseStone =
        stone("tuff", LooseStoneArrangements.TUFF, Identifier.withDefaultNamespace("block/tuff"))

    private fun stone(
        name: String,
        arrangement: LooseStoneArrangement,
        texture: Identifier,
    ): LooseStone = LooseStone(name, arrangement, texture).also(ALL::add)

    public fun init(): Unit = Unit
}
