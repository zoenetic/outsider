package dev.zoenetic.unbidden.survival.datagen.models

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlockStateProperties
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import net.minecraft.client.data.models.BlockModelGenerators.*
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.client.data.models.model.TextureMapping.getBlockTexture
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.CampfireBlock
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

fun createCampfire(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
    topSlot: TextureSlot,
    bottomSlot: TextureSlot,
) {
    val block = UnbiddenBlocks.CAMPFIRE

    val rotation = PropertyDispatch.modify(CampfireBlock.FACING)
        .select(Direction.NORTH, NOP)
        .select(Direction.EAST, Y_ROT_90)
        .select(Direction.SOUTH, Y_ROT_180)
        .select(Direction.WEST, Y_ROT_270)

    // TODO: placeholder — one geometry for every level until the stage table exists.
    val templateFor: (Int) -> Identifier = { level ->
        when (level) {
            else -> Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "block/template_campfire")
        }
    }

    val models: Map<Int, Identifier> = (0..15).associateWith { level ->
        val bottom = (level + 1).coerceAtMost(15)
        val textures = TextureMapping()
            .put(topSlot, getBlockTexture(block, "_fuel_$level"))
            .put(bottomSlot, getBlockTexture(block, "_fuel_$bottom"))
        ModelTemplate(Optional.of(templateFor(level)), Optional.empty(), topSlot, bottomSlot)
            .create(
                ModelLocationUtils.getModelLocation(block, "_fuel_$level"),
                textures,
                modelOutput
            )
    }

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(block)
            .with(
                PropertyDispatch.initial(
                    UnbiddenBlockStateProperties.FUEL_LEVEL,
                )
                    .generate { level ->
                        plainVariant(models.getValue(level))
                    }
            )
            .with(rotation)
    )

    registerSimpleItemModel(block, models.getValue(15), itemModelOutput)
}
