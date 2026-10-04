package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelLocationUtils
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.renderer.item.ItemModel
import net.minecraft.client.renderer.item.properties.select.ComponentContents
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Consumer

fun torchModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
): Identifier {
    val block = OutsiderBlocks.TORCH

    fun template(lit: Boolean): Identifier = Identifier.fromNamespaceAndPath(
        Survival.NAMESPACE,
        "block/template_torch_${if (lit) "lit" else "unlit"}_dead",
    )

    fun models(lit: Boolean): Map<Int, Identifier> = (0..15).associateWith { level ->
        val texture =
            TextureMapping().put(TextureSlot.TORCH, torchTexture(lit, level))
        ModelTemplates.TORCH.createWithSuffix(
            OutsiderBlocks.TORCH,
            "_${if (lit) "lit" else "unlit"}_fuel_$level",
            texture,
            modelOutput,
        )
    }

    val litModels = models(lit = true)
    val unlitModels = models(lit = false)

    val deadModel =
        ModelTemplate(
            Optional.of(template(lit = false)),
            Optional.empty(),
            TextureSlot.TORCH,
        ).create(
            ModelLocationUtils.getModelLocation(block, "_dead"),
            TextureMapping().put(TextureSlot.TORCH, torchTexture(lit = false, 0)),
            modelOutput,
        )

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(
                    OutsiderBlockStateProperties.FUEL_LEVEL,
                    BlockStateProperties.LIT,
                )
                    .generate { level, isLit ->
                        plainVariant(
                            (if (isLit) litModels else unlitModels).getValue(
                                level,
                            ),
                        )
                    },

            ),
    )

    val item = block.asItem()

    fun itemModels(lit: Boolean) = (0..15).associateWith { level ->
        ModelTemplates.FLAT_ITEM.create(
            ModelLocationUtils.getModelLocation(
                item,
                "_${if (lit) "lit" else "unlit"}_fuel_$level",
            ),
            TextureMapping.layer0(torchTexture(lit, level)),
            modelOutput,
        )
    }

    fun unbakedItemModel(lit: Boolean): ItemModel.Unbaked {
        val itemModels = itemModels(lit)
        return ItemModelUtils.select(
            ComponentContents(OutsiderComponents.FUEL_LEVEL),
            ItemModelUtils.plainModel(itemModels.getValue(15)),
            itemModels.map { (level, model) ->
                ItemModelUtils.`when`(Fuel(level), ItemModelUtils.plainModel(model))
            },
        )
    }

    itemModelOutput.accept(
        item,
        ItemModelUtils.conditional(
            ItemModelUtils.hasComponent(OutsiderComponents.LIT),
            unbakedItemModel(lit = true),
            unbakedItemModel(lit = false),
        ),
    )

    return deadModel
}
