package dev.zoenetic.unbidden.survival.datagen.models

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.campfire.CampfireBurn
import dev.zoenetic.unbidden.survival.campfire.CampfireStage
import dev.zoenetic.unbidden.survival.fuel.Fuel
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlockStateProperties
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.client.data.models.model.TextureMapping.getBlockTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

fun campfireModel(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
    topSlot: TextureSlot,
    bottomSlot: TextureSlot,
    emberTopSlot: TextureSlot,
    emberBottomSlot: TextureSlot,
): Identifier {
    val block = UnbiddenBlocks.CAMPFIRE

    fun template(stage: CampfireStage, lit: Boolean): Identifier =
        Identifier.fromNamespaceAndPath(
            Survival.NAMESPACE,
            "block/template_campfire_${if (lit) "lit" else "unlit"}_${stage.id}"
        )

    fun logs(topStep: Int, bottomStep: Int): TextureMapping = TextureMapping()
        .put(topSlot, getBlockTexture(block, "_fuel_$topStep"))
        .put(bottomSlot, getBlockTexture(block, "_fuel_$bottomStep"))

    val litModels: Map<Int, Identifier> = (0..15).associateWith { level ->
        val fuel = Fuel(level)
        val top = CampfireBurn.topStep(fuel)
        val bottom = CampfireBurn.bottomStep(fuel)
        val textures = logs(top, bottom)
            .put(emberTopSlot, getBlockTexture(block, "_ember_$top"))
            .put(emberBottomSlot, getBlockTexture(block, "_ember_$bottom"))
        ModelTemplate(
            Optional.of(template(CampfireBurn.stage(fuel), lit = true)),
            Optional.empty(),
            topSlot,
            bottomSlot,
            emberTopSlot,
            emberBottomSlot,
        ).create(ModelLocationUtils.getModelLocation(block, "_lit_fuel_$level"), textures, modelOutput)
    }

    val unlitModels: Map<Int, Identifier> = (0..15).associateWith { level ->
        val fuel = Fuel(level)
        ModelTemplate(
            Optional.of(template(CampfireBurn.stage(fuel), lit = false)),
            Optional.empty(),
            topSlot,
            bottomSlot,
        ).create(
            ModelLocationUtils.getModelLocation(block, "_unlit_fuel_$level"),
            logs(CampfireBurn.topStep(fuel), CampfireBurn.bottomStep(fuel)),
            modelOutput,
        )
    }

    val deadModel =
        ModelTemplate(Optional.of(template(CampfireStage.SUNK, lit = false)), Optional.empty(), topSlot, bottomSlot)
            .create(ModelLocationUtils.getModelLocation(block, "_dead"), logs(0, 0), modelOutput)

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(UnbiddenBlockStateProperties.FUEL_LEVEL, BlockStateProperties.LIT)
                    .generate { level, isLit ->
                        plainVariant((if (isLit) litModels else unlitModels).getValue(level))
                    }
            )
            .with(horizontalRotation())
    )

    registerSimpleItemModel(block, unlitModels.getValue(15), itemModelOutput)

    return deadModel
}
