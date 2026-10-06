package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.firewood.FirewoodBlock.Companion.BILLETS
import dev.zoenetic.outsider.survival.fuel.firewood.FirewoodBlock.Companion.MAX_BILLETS
import dev.zoenetic.outsider.survival.fuel.firewood.FirewoodBlock.Companion.MIN_BILLETS
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.renderer.block.dispatch.Variant
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.resources.Identifier
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Consumer

fun firewoodModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
    billetSlot: TextureSlot,
) {
    val billet = Material(Identifier.withDefaultNamespace("block/campfire_log"))
    val textures = TextureMapping()
        .put(billetSlot, billet)
        .put(TextureSlot.PARTICLE, billet)

    val models: Map<Int, Identifier> = (MIN_BILLETS..MAX_BILLETS).associateWith { billets ->
        val template = ModelTemplate(
            Optional.of(
                Identifier.fromNamespaceAndPath(
                    Survival.NAMESPACE,
                    "block/template_firewood_$billets",
                ),
            ),
            Optional.empty(),
            billetSlot,
            TextureSlot.PARTICLE,
        )
        template.createWithSuffix(OutsiderBlocks.FIREWOOD, "_$billets", textures, modelOutput)
    }

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(OutsiderBlocks.FIREWOOD)
            .with(
                PropertyDispatch.initial(BILLETS).generate { billets ->
                    variants(Variant(models.getValue(billets)))
                },
            )
            .with(
                horizontalRotation(),
            ),
    )

    itemModelOutput.accept(
        OutsiderItems.FIREWOOD.asItem(),
        ItemModelUtils.plainModel(models.getValue(MIN_BILLETS)),
    )
}
