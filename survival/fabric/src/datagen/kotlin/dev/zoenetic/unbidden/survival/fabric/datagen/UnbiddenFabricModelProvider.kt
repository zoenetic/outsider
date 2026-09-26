package dev.zoenetic.unbidden.survival.fabric.datagen

import dev.zoenetic.unbidden.survival.datagen.models.createCampfire
import dev.zoenetic.unbidden.survival.datagen.models.createFirewood
import dev.zoenetic.unbidden.survival.datagen.models.createTorch
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.model.TextureSlot

class UnbiddenFabricModelProvider(output: FabricPackOutput) : FabricModelProvider(output) {

    override fun generateBlockStateModels(blocks: BlockModelGenerators) {

        createCampfire(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
            TextureSlot.create("top"),
            TextureSlot.create("bottom"),
        )

        createFirewood(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
            TextureSlot.create("billet")
        )

        createTorch(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
        )
    }

    override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {}
}