package dev.zoenetic.outsider.survival.fabric.datagen

import dev.zoenetic.outsider.survival.datagen.models.campfireModel
import dev.zoenetic.outsider.survival.datagen.models.deadCampfireModel
import dev.zoenetic.outsider.survival.datagen.models.firewoodModel
import dev.zoenetic.outsider.survival.datagen.models.torchModel
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

        torchModel(
            blocks.blockStateOutput,
            blocks.modelOutput,
            blocks.itemModelOutput,
        )
    }

    override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {}
}