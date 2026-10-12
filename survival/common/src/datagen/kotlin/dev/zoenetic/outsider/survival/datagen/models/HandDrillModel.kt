package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.resources.Identifier
import java.util.function.BiConsumer

fun handDrillModel(
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
) {
    val item = OutsiderItems.HAND_DRILL
    val model = ModelTemplates.FLAT_ITEM.create(
        item,
        TextureMapping.layer0(item),
        modelOutput,
    )
    itemModelOutput.accept(item, ItemModelUtils.plainModel(model))
}
