package dev.zoenetic.unbidden.survival.datagen.loot

import dev.zoenetic.unbidden.survival.fuel.firewood.FirewoodBlock.Companion.BILLETS
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import net.minecraft.advancements.predicates.StatePropertiesPredicate
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

public fun firewoodLootTable() = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(
                LootItem.lootTableItem(UnbiddenItems.FIREWOOD)
                    .apply(BILLETS.possibleValues) { n ->
                        SetItemCountFunction.setCount(ConstantValue.exactly(n.toFloat()))
                            .`when`(
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                                    UnbiddenBlocks.FIREWOOD
                                ).setProperties(
                                    StatePropertiesPredicate.Builder.properties()
                                        .hasProperty(BILLETS, n)
                                )
                            )
                    }.apply(ApplyExplosionDecay.explosionDecay())
            )
    )
