package dev.zoenetic.unbidden.survival.datagen.models

import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.resources.Identifier
import java.util.function.Consumer

fun deadCampfireModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    model: Identifier
) {
    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            UnbiddenBlocks.DEAD_CAMPFIRE, plainVariant(model)
        ).with(
            horizontalRotation()
        )
    )
}