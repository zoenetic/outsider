package dev.zoenetic.outsider.survival.datagen.loot

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.advancements.predicates.StatePropertiesPredicate.Builder.properties
import net.minecraft.util.Unit
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition.hasBlockStateProperties
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

fun torchLootTable(block: Block) = LootTable.lootTable()
    .withPool(
        LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(
                LootItem.lootTableItem(OutsiderItems.TORCH)
                    .apply(
                        SetComponentsFunction.setComponent(OutsiderComponents.LIT, Unit.INSTANCE)
                            .`when`(
                                hasBlockStateProperties(
                                    block,
                                ).setProperties(
                                    properties().hasProperty(BlockStateProperties.LIT, true),
                                ),
                            ),
                    )
                    .`when`(
                        AnyOfCondition.anyOf(
                            hasBlockStateProperties(
                                block,
                            ).setProperties(properties().hasProperty(FUEL_LEVEL, Fuel.MAX.level)),
                            hasBlockStateProperties(
                                block,
                            ).setProperties(
                                properties().hasProperty(FUEL_LEVEL, Fuel.MAX.level - 1),
                            ),
                        ),
                    )
                    .otherwise(LootItem.lootTableItem(OutsiderItems.DEAD_TORCH)),
            )
            .apply(ApplyExplosionDecay.explosionDecay()),
    )
