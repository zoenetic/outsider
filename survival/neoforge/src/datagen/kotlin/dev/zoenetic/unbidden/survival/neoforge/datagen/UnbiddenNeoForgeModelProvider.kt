package dev.zoenetic.unbidden.survival.neoforge.datagen

import dev.zoenetic.unbidden.survival.datagen.models.createCampfire
import dev.zoenetic.unbidden.survival.datagen.models.createFirewood
import dev.zoenetic.unbidden.survival.datagen.models.createTorch
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.ModelProvider
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.stream.Stream

class UnbiddenNeoForgeModelProvider(output: PackOutput, modId: String) :
    ModelProvider(output, modId) {

    override fun getKnownBlocks(): Stream<out Holder<Block>> =
        Stream.of(
            BuiltInRegistries.BLOCK.wrapAsHolder(UnbiddenBlocks.FIREWOOD),
            BuiltInRegistries.BLOCK.wrapAsHolder(UnbiddenBlocks.TORCH),
            BuiltInRegistries.BLOCK.wrapAsHolder(UnbiddenBlocks.WALL_TORCH),
        )

    override fun getKnownItems(): Stream<out Holder<Item>> =
        Stream.of(
            BuiltInRegistries.ITEM.wrapAsHolder(UnbiddenItems.FIREWOOD),
            BuiltInRegistries.ITEM.wrapAsHolder(UnbiddenItems.TORCH),
        )

    override fun registerModels(blocks: BlockModelGenerators, items: ItemModelGenerators) {

        createCampfire(
            blocks.blockStateOutput,
            blocks.modelOutput,
            items.itemModelOutput,
            TextureSlot.create("top"),
            TextureSlot.create("bottom")
        )

        createFirewood(
            blocks.blockStateOutput,
            blocks.modelOutput,
            items.itemModelOutput,
            TextureSlot.create("billet")
        )

        createTorch(
            blocks.blockStateOutput,
            items.modelOutput,
            items.itemModelOutput
        )
    }
}