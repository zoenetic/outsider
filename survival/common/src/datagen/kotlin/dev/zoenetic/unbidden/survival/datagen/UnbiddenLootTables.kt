package dev.zoenetic.unbidden.survival.datagen

import dev.zoenetic.unbidden.survival.datagen.loot.campfireLootTable
import dev.zoenetic.unbidden.survival.datagen.loot.deadCampfireLootTable
import dev.zoenetic.unbidden.survival.datagen.loot.firewoodLootTable
import dev.zoenetic.unbidden.survival.datagen.loot.torchLootTable
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
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

class UnbiddenLootTables : LootTableSubProvider {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {

        output.accept(
            UnbiddenBlocks.CAMPFIRE.lootTable.orElseThrow(),
            campfireLootTable()
        )

        output.accept(
            UnbiddenBlocks.DEAD_CAMPFIRE.lootTable.orElseThrow(),
            deadCampfireLootTable()
        )

        output.accept(
            UnbiddenBlocks.FIREWOOD.lootTable.orElseThrow(),
            firewoodLootTable()
        )

        output.accept(
            UnbiddenBlocks.TORCH.lootTable.orElseThrow(),
            torchLootTable()
        )
    }


    companion object {
        fun factory(
            output: PackOutput,
            registries: CompletableFuture<HolderLookup.Provider>
        ): DataProvider = LootTableProvider(
            output,
            emptySet(),
            listOf(
                LootTableProvider.SubProviderEntry(
                    { UnbiddenLootTables() },
                    LootContextParamSets.BLOCK,
                )
            ),
            registries
        )
    }
}
