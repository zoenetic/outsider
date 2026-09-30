package dev.zoenetic.outsider.survival.torch

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

public class OutsiderDeadWallTorchBlock(properties: Properties) :
    OutsiderDeadTorchBlock(properties) {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
        )
    }

    public override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean =
        WallTorchBlock.canSurvive(level, pos, state.getValue(HORIZONTAL_FACING))

    override fun codec(): MapCodec<out BaseTorchBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(HORIZONTAL_FACING)
    }

    public override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = SHAPES.getValue(state.getValue(HORIZONTAL_FACING))

    public override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        for (direction in context.nearestLookingDirections) {
            if (!direction.axis.isHorizontal) continue
            val state = defaultBlockState().setValue(HORIZONTAL_FACING, direction.opposite)
            if (state.canSurvive(context.level, context.clickedPos)) return state
        }
        return null
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)))

    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)))

    protected override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbor: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: RandomSource,
    ): BlockState {
        return if (directionToNeighbor.opposite == state.getValue(HORIZONTAL_FACING) && !state.canSurvive(
                level, pos
            )
        ) {
            Blocks.AIR.defaultBlockState()
        } else {
            state
        }
    }

    public companion object {
        public val CODEC: MapCodec<OutsiderDeadWallTorchBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<OutsiderDeadWallTorchBlock> ->
                i.group(
                    Properties.CODEC.fieldOf("properties").forGetter { b -> b.properties }
                ).apply(i, ::OutsiderDeadWallTorchBlock)
            }

        internal val SHAPES = Shapes.rotateHorizontal(Block.boxZ(5.0, 3.0, 12.0, 11.0, 16.0))
    }

}