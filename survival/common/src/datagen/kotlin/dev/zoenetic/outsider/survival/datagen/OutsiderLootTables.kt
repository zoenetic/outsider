package dev.zoenetic.outsider.survival.datagen

import dev.zoenetic.outsider.survival.datagen.loot.campfireLootTable
import dev.zoenetic.outsider.survival.datagen.loot.deadCampfireLootTable
import dev.zoenetic.outsider.survival.datagen.loot.deadTorchLootTable
import dev.zoenetic.outsider.survival.datagen.loot.firewoodLootTable
import dev.zoenetic.outsider.survival.datagen.loot.looseStoneLootTable
import dev.zoenetic.outsider.survival.datagen.loot.torchLootTable
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class OutsiderLootTables : LootTableSubProvider {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
        output.accept(
            OutsiderBlocks.CAMPFIRE.lootTable.orElseThrow(),
            campfireLootTable(),
        )

        output.accept(
            OutsiderBlocks.DEAD_CAMPFIRE.lootTable.orElseThrow(),
            deadCampfireLootTable(),
        )

        output.accept(
            OutsiderBlocks.FIREWOOD.lootTable.orElseThrow(),
            firewoodLootTable(),
        )

        output.accept(
            OutsiderBlocks.LOOSE_STONE.lootTable.orElseThrow(),
            looseStoneLootTable(),
        )

        output.accept(
            OutsiderBlocks.TORCH.lootTable.orElseThrow(),
            torchLootTable(),
        )

        output.accept(
            OutsiderBlocks.DEAD_TORCH.lootTable.orElseThrow(),
            deadTorchLootTable(),
        )
    }

    companion object {
        fun factory(
            output: PackOutput,
            registries: CompletableFuture<HolderLookup.Provider>,
        ): DataProvider = LootTableProvider(
            output,
            emptySet(),
            listOf(
                LootTableProvider.SubProviderEntry(
                    { OutsiderLootTables() },
                    LootContextParamSets.BLOCK,
                ),
            ),
            registries,
        )
    }
}
