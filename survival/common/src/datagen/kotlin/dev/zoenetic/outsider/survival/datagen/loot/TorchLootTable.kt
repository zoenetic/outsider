package dev.zoenetic.outsider.survival.datagen.loot

import dev.zoenetic.outsider.survival.fuel.CopyFuelStateFunction
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

fun torchLootTable() = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(
                LootItem.lootTableItem(OutsiderBlocks.TORCH).apply(CopyFuelStateFunction.builder()),
            )
            .`when`(ExplosionCondition.survivesExplosion()),
    )
