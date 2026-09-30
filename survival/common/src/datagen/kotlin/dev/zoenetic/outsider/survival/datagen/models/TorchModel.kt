package dev.zoenetic.outsider.survival.datagen.models

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.OutsiderBlockStateProperties
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

fun torchModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
): Identifier {
    val block = OutsiderBlocks.TORCH

    fun template(lit: Boolean): Identifier =
        Identifier.fromNamespaceAndPath(
            Survival.NAMESPACE,
            "block/template_torch_${if (lit) "lit" else "unlit"}"
        )


    val litModels: Map<Int, Identifier> = (0..15).associateWith { level ->
        val texture =
            TextureMapping().put(TextureSlot.TORCH, torchTexture(lit = true, level))
        ModelTemplates.TORCH.createWithSuffix(
            OutsiderBlocks.TORCH,
            "_lit_fuel_$level",
            texture,
            modelOutput,
        )
    }

    val unlitModels: Map<Int, Identifier> = (0..15).associateWith { level ->
        val texture =
            TextureMapping().put(TextureSlot.TORCH, torchTexture(lit = false, level))
        ModelTemplates.TORCH.createWithSuffix(
            OutsiderBlocks.TORCH,
            "_unlit_fuel_$level",
            texture,
            modelOutput,
        )
    }

    val deadModel =
        ModelTemplate(
            Optional.of(template(lit = false)),
            Optional.empty(),
            TextureSlot.TORCH
        ).create(
            ModelLocationUtils.getModelLocation(block, "_dead"),
            TextureMapping().put(TextureSlot.TORCH, torchTexture(lit = false, 0)),
            modelOutput
        )

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(
                    OutsiderBlockStateProperties.FUEL_LEVEL,
                    BlockStateProperties.LIT
                )
                    .generate { level, isLit ->
                        plainVariant((if (isLit) litModels else unlitModels).getValue(level))
                    }

            )
    )

    val item = block.asItem()

    val litItemModel = ModelTemplates.FLAT_ITEM.create(
        ModelLocationUtils.getModelLocation(item, "_lit"),
        TextureMapping.layer0(torchTexture(lit = true, 15)),
        modelOutput
    )

    val unlitItemModel = ModelTemplates.FLAT_ITEM.create(
        ModelLocationUtils.getModelLocation(item, "_unlit"),
        TextureMapping.layer0(torchTexture(lit = false, 15)),
        modelOutput
    )

    itemModelOutput.accept(
        item,
        ItemModelUtils.conditional(
            ItemModelUtils.hasComponent(OutsiderComponents.LIT),
            ItemModelUtils.plainModel(litItemModel),
            ItemModelUtils.plainModel(unlitItemModel)
        )
    )

    return deadModel
}