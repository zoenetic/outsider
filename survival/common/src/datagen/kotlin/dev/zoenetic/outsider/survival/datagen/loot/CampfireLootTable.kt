package dev.zoenetic.outsider.survival.datagen.loot

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.advancements.predicates.StatePropertiesPredicate
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider

private fun campfirePool(
    item: Item,
    count: (remaining: Float) -> NumberProvider,
): LootPool.Builder {
    val pool = LootPool.lootPool()
        .setRolls(ConstantValue.exactly(1f))
        .`when`(ExplosionCondition.survivesExplosion())
    for (level in FUEL_LEVEL.possibleValues) {
        pool.add(
            LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(count(level / Fuel.MAX.level.toFloat())))
                .`when`(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(
                        OutsiderBlocks.CAMPFIRE,
                    ).setProperties(
                        StatePropertiesPredicate.Builder.properties()
                            .hasProperty(FUEL_LEVEL, level),
                    ),
                ),
        )
    }
    return pool
}

private fun campfireFirewood(remaining: Float): NumberProvider =
    BinomialDistributionGenerator.binomial(3, remaining)

private fun campfireCharcoal(remaining: Float): NumberProvider =
    BinomialDistributionGenerator.binomial(2, (1f - remaining) / 2f)

public fun campfireLootTable() = LootTable.lootTable()
    .withPool(campfirePool(OutsiderItems.FIREWOOD, ::campfireFirewood))
    .withPool(campfirePool(Items.CHARCOAL, ::campfireCharcoal))
