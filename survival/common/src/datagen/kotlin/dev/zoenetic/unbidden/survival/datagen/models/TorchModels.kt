package dev.zoenetic.unbidden.survival.datagen.models

import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import java.util.function.BiConsumer
import java.util.function.Consumer

fun createTorch(
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
) {
    val rotation = PropertyDispatch.modify(HORIZONTAL_FACING)
        .select(Direction.EAST, BlockModelGenerators.NOP)
        .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_90)
        .select(Direction.WEST, BlockModelGenerators.Y_ROT_180)
        .select(Direction.NORTH, BlockModelGenerators.Y_ROT_270)

    val particleMaterial = Material(Identifier.withDefaultNamespace("block/oak_log"))

    val litMaterial = Material(Identifier.withDefaultNamespace("block/torch"))
    val litTextures = TextureMapping().put(TextureSlot.TORCH, litMaterial)
        .putForced(TextureSlot.PARTICLE, particleMaterial)

    val unlitMaterial = Material(Identifier.withDefaultNamespace("block/torch"))
    val unlitTextures = TextureMapping().put(TextureSlot.TORCH, unlitMaterial)
        .putForced(TextureSlot.PARTICLE, particleMaterial)

    val groundModelLit =
        plainVariant(ModelTemplates.TORCH.create(UnbiddenBlocks.TORCH, litTextures, modelOutput))

    val groundModelUnlit =
        plainVariant(
            ModelTemplates.TORCH.createWithSuffix(
                UnbiddenBlocks.TORCH,
                "_unlit",
                unlitTextures,
                modelOutput
            )
        )

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(UnbiddenBlocks.TORCH).with(
            createBooleanModelDispatch(
                BlockStateProperties.LIT,
                groundModelLit,
                groundModelUnlit,
            )
        )
    )

    val wallModelOn =
        plainVariant(
            ModelTemplates.WALL_TORCH.create(
                UnbiddenBlocks.WALL_TORCH,
                litTextures,
                modelOutput
            )
        )

    val wallModelOff =
        plainVariant(
            ModelTemplates.WALL_TORCH.createWithSuffix(
                UnbiddenBlocks.WALL_TORCH,
                "_unlit",
                unlitTextures,
                modelOutput
            )
        )

    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(UnbiddenBlocks.WALL_TORCH).with(
            createBooleanModelDispatch(
                BlockStateProperties.LIT,
                wallModelOn,
                wallModelOff
            )
        ).with(rotation)
    )

    registerSimpleFlatItemModel(UnbiddenBlocks.TORCH, itemModelOutput, modelOutput)
}