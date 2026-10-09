package dev.zoenetic.outsider.survival.datagen.loot

import dev.zoenetic.outsider.survival.registry.blocks.OutsiderLooseStoneBlocks
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.STONES
import net.minecraft.advancements.predicates.StatePropertiesPredicate
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

public fun looseStoneLootTable(item: Item) = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(
                LootItem.lootTableItem(item)
                    .apply(STONES.possibleValues) { n ->
                        SetItemCountFunction.setCount(ConstantValue.exactly(n.toFloat()))
                            .`when`(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                    OutsiderLooseStoneBlocks.STONE,
                                ).setProperties(
                                    StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(STONES, n),
                                ),
                            )
                    }.apply(ApplyExplosionDecay.explosionDecay()),
            ),
    )
