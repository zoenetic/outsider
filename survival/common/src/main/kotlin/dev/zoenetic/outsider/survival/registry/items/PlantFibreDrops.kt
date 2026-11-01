package dev.zoenetic.outsider.survival.registry.items

import net.minecraft.advancements.predicates.ItemPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.level.storage.loot.predicates.MatchTool

public object PlantFibreDrops {
    private const val CHANCE: Float = 0.25F

    public val TABLES: Set<ResourceKey<LootTable>> by lazy {
        setOf(Blocks.SHORT_GRASS, Blocks.FERN).map { it.lootTable.orElseThrow() }.toSet()
    }

    public fun pool(registries: HolderLookup.Provider): LootPool {
        val items = registries.lookupOrThrow(Registries.ITEM)
        return LootPool.lootPool()
            .add(LootItem.lootTableItem(OutsiderItems.PLANT_FIBRE))
            .`when`(LootItemRandomChanceCondition.randomChance(CHANCE))
            .`when`(
                InvertedLootItemCondition.invert(
                    MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, Items.SHEARS)),
                ),
            )
            .build()
    }
}
