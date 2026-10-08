package dev.zoenetic.outsider.survival.datagen.models.torch

import dev.zoenetic.outsider.survival.datagen.models.plainVariant
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.resources.Identifier
import java.util.function.Consumer

fun deadTorchModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    itemModelOutput: ItemModelOutput,
    model: Identifier,
) {
    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(
            OutsiderBlocks.DEAD_TORCH,
            plainVariant(model),
        ),
    )

    itemModelOutput.accept(
        OutsiderItems.DEAD_TORCH.asItem(),
        ItemModelUtils.plainModel(model),
    )
}
