package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.client.InLightingPhase
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelLocationUtils
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Items
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

    itemModelOutput.accept(
        item,
        ItemModelUtils.conditional(
            InLightingPhase(LightingFireState.Phase.Kind.DRILLING),
            ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(Items.STICK)),
            ItemModelUtils.plainModel(model),
        ),
    )

    ModelTemplates.FLAT_ITEM.create(
        ModelLocationUtils.getModelLocation(item, "_hearth_board"),
        TextureMapping.layer0(TextureMapping.getItemTexture(item, "_hearth_board")),
        modelOutput,
    )

    ModelTemplates.FLAT_ITEM.create(
        ModelLocationUtils.getModelLocation(item, "_hearth_board_ember"),
        TextureMapping.layer0(TextureMapping.getItemTexture(item, "_hearth_board_ember")),
        modelOutput,
    )
}
