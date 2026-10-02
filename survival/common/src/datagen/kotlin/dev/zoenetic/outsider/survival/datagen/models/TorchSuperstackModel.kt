package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelLocationUtils

fun torchSuperstackModel(itemModelOutput: ItemModelOutput) {
    itemModelOutput.accept(
        OutsiderItems.TORCH_SUPERSTACK,
        ItemModelUtils.conditional(
            ItemModelUtils.hasComponent(OutsiderComponents.LIT),
            ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(OutsiderItems.TORCH, "_lit_fuel_15")),
            ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(OutsiderItems.TORCH, "_unlit_fuel_15"))
        )
    )
}
