package dev.zoenetic.outsider.survival.fabric.datagen

import dev.zoenetic.outsider.survival.datagen.models.*
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.model.TextureSlot

class OutsiderFabricModelProvider(output: FabricPackOutput) : FabricModelProvider(output) {

    override fun generateBlockStateModels(blocks: BlockModelGenerators) {

        val campfire0 = campfireModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
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
            blocks.itemModelOutput,
            TextureSlot.create("billet")
        )

        val torch0 = torchModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
        )

        deadTorchModel(
            blocks.blockStateOutput,
            torch0,
        )

        val wallTorch0 = wallTorchModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
        )

        deadWallTorchModel(
            blocks.blockStateOutput,
            wallTorch0,
        )
    }

    override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {
        torchSuperstackModel(itemModelGenerators.itemModelOutput)
    }
}