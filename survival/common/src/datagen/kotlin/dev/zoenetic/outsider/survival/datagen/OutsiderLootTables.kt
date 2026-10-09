package dev.zoenetic.outsider.survival.datagen

import dev.zoenetic.outsider.survival.datagen.loot.campfireLootTable
import dev.zoenetic.outsider.survival.datagen.loot.deadCampfireLootTable
import dev.zoenetic.outsider.survival.datagen.loot.deadTorchLootTable
import dev.zoenetic.outsider.survival.datagen.loot.firewoodLootTable
import dev.zoenetic.outsider.survival.datagen.loot.looseStoneLootTable
import dev.zoenetic.outsider.survival.datagen.loot.torchLootTable
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderLooseStoneBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderLooseStoneItems
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
            OutsiderLooseStoneBlocks.ANDESITE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.ANDESITE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.BASALT.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.BASALT),
        )

        output.accept(
            OutsiderLooseStoneBlocks.BLACKSTONE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.BLACKSTONE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.CALCITE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.CALCITE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.DIORITE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.DIORITE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.DEEPSLATE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.DEEPSLATE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.ENDSTONE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.ENDSTONE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.GRANITE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.GRANITE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.RED_SANDSTONE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.RED_SANDSTONE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.SANDSTONE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.SANDSTONE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.STONE.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.STONE),
        )

        output.accept(
            OutsiderLooseStoneBlocks.TUFF.lootTable.orElseThrow(),
            looseStoneLootTable(OutsiderLooseStoneItems.TUFF),
        )

        output.accept(
            OutsiderBlocks.TORCH.lootTable.orElseThrow(),
            torchLootTable(OutsiderBlocks.TORCH),
        )

        output.accept(
            OutsiderBlocks.WALL_TORCH.lootTable.orElseThrow(),
            torchLootTable(OutsiderBlocks.WALL_TORCH),
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
