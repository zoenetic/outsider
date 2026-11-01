package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.fire.LightingFireState.Phase.Kind
import dev.zoenetic.outsider.survival.fire.client.EmberHeat
import dev.zoenetic.outsider.survival.fire.client.InLightingPhase
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelLocationUtils
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.resources.Identifier
import java.util.function.BiConsumer

fun tinderModel(
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
) {
    val item = OutsiderItems.TINDER

    fun model(suffix: String) = ItemModelUtils.plainModel(
        ModelTemplates.FLAT_ITEM.create(
            ModelLocationUtils.getModelLocation(item, "_$suffix"),
            TextureMapping.layer0(TextureMapping.getItemTexture(item, "_$suffix")),
            modelOutput,
        ),
    )

    val embers = (0 until EMBER_STAGES).map { model("ember_$it") }
    val stages = embers.mapIndexed { stage, ember ->
        ItemModelUtils.override(ember, stage.toFloat() / EMBER_STAGES)
    }

    itemModelOutput.accept(
        item,
        ItemModelUtils.conditional(
            ItemModelUtils.hasComponent(OutsiderComponents.LIT),
            model("lit"),
            ItemModelUtils.conditional(
                InLightingPhase(Kind.NURSING_EMBERS),
                ItemModelUtils.rangeSelect(EmberHeat, embers.first(), *stages.toTypedArray()),
                model("unlit"),
            ),
        ),
    )
}

private const val EMBER_STAGES = 4
