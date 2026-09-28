package dev.zoenetic.outsider.survival.datagen.models

import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.*
import net.minecraft.client.renderer.block.dispatch.Variant
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.random.Weighted
import net.minecraft.util.random.WeightedList
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.level.block.state.properties.BooleanProperty
import java.util.*
import java.util.function.BiConsumer

fun plainModel(model: Identifier): Variant = Variant(model)
fun variant(variant: Variant) = MultiVariant(WeightedList.of(variant))
fun variants(vararg variant: Variant) =
    MultiVariant(WeightedList.of(Arrays.stream(variant).map { v -> Weighted(v, 1) }.toList()))

fun plainVariant(model: Identifier) = variant(plainModel(model))

fun createBooleanModelDispatch(
    property: BooleanProperty,
    onTrue: MultiVariant,
    onFalse: MultiVariant
) = PropertyDispatch.initial(property).select(true, onTrue).select(false, onFalse)

fun registerSimpleFlatItemModel(
    block: Block,
    itemModelOutput: ItemModelOutput,
    modelOutput: BiConsumer<Identifier, ModelInstance>
) {
    if (block != Blocks.AIR) registerSimpleItemModel(
        block,
        createFlatItemModelWithBlockTexture(block, modelOutput),
        itemModelOutput,
    )
}

fun registerSimpleItemModel(block: Block, model: Identifier, itemModelOutput: ItemModelOutput) =
    itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model))

fun createFlatItemModelWithBlockTexture(
    block: Block,
    modelOutput: BiConsumer<Identifier, ModelInstance>
) = ModelTemplates.FLAT_ITEM.create(
    ModelLocationUtils.getModelLocation(block.asItem()),
    TextureMapping.layer0(block),
    modelOutput
)

fun horizontalRotation() = PropertyDispatch.modify(HORIZONTAL_FACING)
    .select(Direction.NORTH, BlockModelGenerators.Y_ROT_180)
    .select(Direction.EAST, BlockModelGenerators.Y_ROT_270)
    .select(Direction.SOUTH, BlockModelGenerators.NOP)
    .select(Direction.WEST, BlockModelGenerators.Y_ROT_90)