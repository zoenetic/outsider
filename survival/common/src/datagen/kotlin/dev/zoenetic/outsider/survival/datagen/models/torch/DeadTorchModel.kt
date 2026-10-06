package dev.zoenetic.outsider.survival.datagen.models.torch

import dev.zoenetic.outsider.survival.datagen.models.plainVariant
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.resources.Identifier
import java.util.function.Consumer

fun deadTorchModel(blockStateOutput: Consumer<BlockModelDefinitionGenerator>, model: Identifier) {
    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            OutsiderBlocks.DEAD_TORCH,
            plainVariant(model),
        ),
    )
}
