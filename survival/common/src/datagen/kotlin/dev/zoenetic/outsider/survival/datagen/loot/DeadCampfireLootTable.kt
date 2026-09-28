package dev.zoenetic.outsider.survival.datagen.loot

import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

public fun deadCampfireLootTable() = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .`when`(ExplosionCondition.survivesExplosion())
            .add(
                LootItem.lootTableItem(Items.CHARCOAL)
                    .apply(
                        SetItemCountFunction.setCount(
                            BinomialDistributionGenerator.binomial(
                                2,
                                0.5f
                            )
                        )
                    )
            )
    )