package dev.zoenetic.outsider.survival.stone.loose

import com.mojang.serialization.MapCodec
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.MAX_STONES
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.forCount
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

public class LooseStoneBlock(properties: Properties) :
    Block(properties),
    SimpleWaterloggedBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(STONES, 1)
                .setValue(WATERLOGGED, false),
        )
    }

    override fun canBeReplaced(state: BlockState, context: BlockPlaceContext): Boolean = (
        !context.isSecondaryUseActive && context.itemInHand
            .`is`(asItem()) &&
            state.getValue(STONES) < MAX_STONES
        ) || super.canBeReplaced(state, context)

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val belowPos = pos.below()
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)
    }

    override fun codec(): MapCodec<out Block> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(STONES).add(WATERLOGGED)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape {
        val shapes = SHAPES_BY_COUNT[state.getValue(STONES) - 1]
        val index = RandomSource.createThreadLocalInstance(state.getSeed(pos)).nextInt(shapes.size)
        return shapes[index]
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val state = context.level.getBlockState(context.clickedPos)
        if (state.`is`(this)) return state.cycle(STONES)
        val replacedFluidState = context.level.getFluidState(context.clickedPos)
        val isWaterSource = replacedFluidState.`is`(Fluids.WATER)
        return super.getStateForPlacement(context)
            ?.setValue(WATERLOGGED, isWaterSource)
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource,
    ): BlockState {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState()
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(
                pos,
                Fluids.WATER,
                Fluids.WATER.getTickDelay(level),
            )
        }
        return super.updateShape(
            state,
            level,
            ticks,
            pos,
            directionToNeighbour,
            neighbourPos,
            neighbourState,
            random,
        )
    }

    override fun getFluidState(state: BlockState): FluidState = if (state.getValue(WATERLOGGED)) {
        Fluids.WATER.getSource(false)
    } else {
        super.getFluidState(state)
    }

    public companion object {
        public val CODEC: MapCodec<LooseStoneBlock> = simpleCodec(::LooseStoneBlock)

        private val SHAPES_BY_COUNT: List<List<VoxelShape>> =
            (1..MAX_STONES).map { count ->
                val arrangements = forCount(count)
                val shapes = arrangements.distinct().associateWith { arrangement ->
                    arrangement.map { it.toShape() }.reduce(Shapes::or)
                }
                arrangements.map(shapes::getValue)
            }

        public val STONES: IntegerProperty = IntegerProperty.create(
            "stones",
            1,
            MAX_STONES,
        )

        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        public fun LooseStoneArrangements.Box.toShape(): VoxelShape = Block.box(
            minX.toDouble(),
            0.0,
            minZ.toDouble(),
            maxX.toDouble(),
            height.toDouble(),
            maxZ.toDouble(),
        )
    }
}
