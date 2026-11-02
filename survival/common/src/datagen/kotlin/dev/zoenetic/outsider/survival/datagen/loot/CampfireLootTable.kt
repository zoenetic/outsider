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
import net.minecraft.world.level.storage.loot.providers.number.Sum

private val GRACE_LEVELS = Fuel.MAX.level - 1..Fuel.MAX.level
private val LAST_EMBERS = 1F / Fuel.MAX.level

private fun atFuel(level: Int) =
    LootItemBlockStatePropertyCondition.hasBlockStateProperties((OutsiderBlocks.CAMPFIRE))
        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(FUEL_LEVEL, level))

private fun campfirePool(
    item: Item,
    count: (remaining: Float) -> NumberProvider,
): LootPool.Builder {
    val pool = LootPool.lootPool()
        .setRolls(ConstantValue.exactly(1F))
        .`when`(ExplosionCondition.survivesExplosion())
    for (level in FUEL_LEVEL.possibleValues) {
        if (level in GRACE_LEVELS) continue
        pool.add(
            LootItem.lootTableItem(item)
                .apply(SetItemCountFunction.setCount(count(level / Fuel.MAX.level.toFloat())))
                .`when`(atFuel(level)),
        )
    }
    return pool
}

private fun wholeCampfirePool(): LootPool.Builder {
    val pool = LootPool.lootPool()
        .setRolls(ConstantValue.exactly(1F))
        .`when`(ExplosionCondition.survivesExplosion())
    for (level in GRACE_LEVELS) {
        pool.add(LootItem.lootTableItem(OutsiderItems.CAMPFIRE).`when`(atFuel(level)))
    }
    return pool
}

private fun campfireFirewood(remaining: Float): NumberProvider =
    BinomialDistributionGenerator.binomial(3, remaining)

private fun campfireCharcoal(remaining: Float): NumberProvider =
    if (remaining <= LAST_EMBERS) {
        Sum.sum(ConstantValue.exactly(1F), DEAD_CAMPFIRE_EXTRA_CHARCOAL)
    } else {
        BinomialDistributionGenerator.binomial(2, (1F - remaining) / 2F)
    }

public fun campfireLootTable(): LootTable.Builder = LootTable.lootTable()
    .withPool(campfirePool(OutsiderItems.FIREWOOD, ::campfireFirewood))
    .withPool(campfirePool(Items.CHARCOAL, ::campfireCharcoal))
    .withPool(wholeCampfirePool())
