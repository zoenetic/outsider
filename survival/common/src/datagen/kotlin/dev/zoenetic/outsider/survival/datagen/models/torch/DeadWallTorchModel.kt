package dev.zoenetic.outsider.survival.datagen.models.torch

import dev.zoenetic.outsider.survival.datagen.models.plainVariant
import dev.zoenetic.outsider.survival.datagen.models.reversedHorizontalRotation
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.resources.Identifier
import java.util.function.Consumer

fun deadWallTorchModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    model: Identifier,
) {
    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            OutsiderBlocks.DEAD_WALL_TORCH,
            plainVariant(model),
        ).with(
            reversedHorizontalRotation(),
        ),
    )
}
