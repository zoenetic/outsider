package dev.zoenetic.outsider.survival.datagen.models

import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.renderer.block.dispatch.Variant
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.util.random.Weighted
import net.minecraft.util.random.WeightedList
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING

fun plainModel(model: Identifier): Variant = Variant(model)
fun variant(variant: Variant) = MultiVariant(WeightedList.of(variant))
fun variants(vararg variant: Variant) = variants(variant.asList())

fun variants(variants: List<Variant>) =
    MultiVariant(WeightedList.of(variants.map { Weighted(it, 1) }))

fun plainVariant(model: Identifier) = variant(plainModel(model))

fun registerSimpleItemModel(block: Block, model: Identifier, itemModelOutput: ItemModelOutput) =
    itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model))

fun horizontalRotation() = PropertyDispatch.modify(HORIZONTAL_FACING)
    .select(Direction.NORTH, BlockModelGenerators.Y_ROT_180)
    .select(Direction.EAST, BlockModelGenerators.Y_ROT_270)
    .select(Direction.SOUTH, BlockModelGenerators.NOP)
    .select(Direction.WEST, BlockModelGenerators.Y_ROT_90)

fun reversedHorizontalRotation() = PropertyDispatch.modify(HORIZONTAL_FACING)
    .select(Direction.EAST, BlockModelGenerators.NOP)
    .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_90)
    .select(Direction.WEST, BlockModelGenerators.Y_ROT_180)
    .select(Direction.NORTH, BlockModelGenerators.Y_ROT_270)
