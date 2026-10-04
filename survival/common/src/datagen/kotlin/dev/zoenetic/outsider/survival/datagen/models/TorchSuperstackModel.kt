package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput

fun torchSuperstackModel(itemModelOutput: ItemModelOutput) {
    itemModelOutput.copy(OutsiderItems.TORCH, OutsiderItems.TORCH_SUPERSTACK)
}
