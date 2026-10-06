package dev.zoenetic.outsider.survival.datagen.loot

import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.STONES
import net.minecraft.advancements.predicates.StatePropertiesPredicate
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

public fun looseStoneLootTable() = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(
                LootItem.lootTableItem(OutsiderItems.LOOSE_STONE)
                    .apply(STONES.possibleValues) { n ->
                        SetItemCountFunction.setCount(ConstantValue.exactly(n.toFloat()))
                            .`when`(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                    OutsiderBlocks.LOOSE_STONE,
                                ).setProperties(
                                    StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(STONES, n),
                                ),
                            )
                    }.apply(ApplyExplosionDecay.explosionDecay()),
            ),
    )
