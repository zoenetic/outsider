package dev.zoenetic.outsider.survival.datagen.loot

import net.minecraft.world.item.Items
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

public fun deadTorchLootTable() = LootTable.lootTable().withPool(
    LootPool.lootPool()
        .setRolls(ConstantValue.exactly(1f))
        .add(LootItem.lootTableItem(Items.STICK))
        .`when`(ExplosionCondition.survivesExplosion())
)