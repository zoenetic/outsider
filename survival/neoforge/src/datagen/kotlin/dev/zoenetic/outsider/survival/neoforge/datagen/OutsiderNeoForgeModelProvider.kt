package dev.zoenetic.outsider.survival.neoforge.datagen

import dev.zoenetic.outsider.survival.datagen.models.*
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderItems
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

class OutsiderNeoForgeModelProvider(output: PackOutput, modId: String) :
    ModelProvider(output, modId) {

    override fun getKnownBlocks(): Stream<out Holder<Block>> =
        Stream.of(
            BuiltInRegistries.BLOCK.wrapAsHolder(OutsiderBlocks.CAMPFIRE),
            BuiltInRegistries.BLOCK.wrapAsHolder(OutsiderBlocks.DEAD_CAMPFIRE),
            BuiltInRegistries.BLOCK.wrapAsHolder(OutsiderBlocks.FIREWOOD),
            BuiltInRegistries.BLOCK.wrapAsHolder(OutsiderBlocks.TORCH),
            BuiltInRegistries.BLOCK.wrapAsHolder(OutsiderBlocks.WALL_TORCH),
        )

    override fun getKnownItems(): Stream<out Holder<Item>> =
        Stream.of(
            BuiltInRegistries.ITEM.wrapAsHolder(OutsiderItems.CAMPFIRE),
            BuiltInRegistries.ITEM.wrapAsHolder(OutsiderItems.FIREWOOD),
            BuiltInRegistries.ITEM.wrapAsHolder(OutsiderItems.TORCH),
            BuiltInRegistries.ITEM.wrapAsHolder(OutsiderItems.TORCH_SUPERSTACK),
        )

    override fun registerModels(blocks: BlockModelGenerators, items: ItemModelGenerators) {

        val campfire0 = campfireModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            items.itemModelOutput,
            TextureSlot.create("top"),
            TextureSlot.create("bottom"),
            TextureSlot.create("ember_top"),
            TextureSlot.create("ember_bottom"),
        )

        deadCampfireModel(
            blocks.blockStateOutput,
            campfire0,
        )

        firewoodModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            items.itemModelOutput,
            TextureSlot.create("billet")
        )

        val torch0 = torchModel(
            blocks.blockStateOutput,
            items.modelOutput,
            items.itemModelOutput
        )

        val wallTorch0 = wallTorchModel(
            blocks.blockStateOutput,
            items.modelOutput,
            items.itemModelOutput
        )

        deadTorchModel(
            blocks.blockStateOutput,
            torch0,
        )

        deadWallTorchModel(
            blocks.blockStateOutput,
            wallTorch0,
        )

        torchSuperstackModel(items.itemModelOutput)
    }
}