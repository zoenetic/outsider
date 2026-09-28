package dev.zoenetic.outsider.survival.fuel.firewood

import com.mojang.math.OctahedralGroup
import com.mojang.serialization.MapCodec
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.FluidTags.WATER
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

public class FirewoodBlock(properties: Properties) : Block(properties), SimpleWaterloggedBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.NORTH)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, BILLETS, WATERLOGGED)
    }

    override fun codec(): MapCodec<FirewoodBlock> = CODEC

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hit: BlockHitResult
    ): InteractionResult {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            val billets = state.getValue(BILLETS)
            if (billets > 1) {
                level.setBlock(pos, state.setValue(BILLETS, billets - 1), 3)
            } else {
                level.setBlock(
                    pos,
                    state.fluidState.createLegacyBlock(),
                    3
                )
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos)
            }
            val taken = ItemStack(OutsiderItems.FIREWOOD, 1)
            level.playSound(player, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1F, 1F)
            if (!player.inventory.add(taken)) player.drop(taken, false)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    override fun canBeReplaced(state: BlockState, context: BlockPlaceContext): Boolean {
        return (!context.isSecondaryUseActive && context.itemInHand
            .item == this.asItem()
                && state.getValue(BILLETS) < 12) || super.canBeReplaced(state, context)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val state = context.level.getBlockState(context.clickedPos)
        if (state.`is`(this)) return state.cycle(BILLETS)
        val replacedFluidState = context.level.getFluidState(context.clickedPos)
        val isWaterSource = replacedFluidState.`is`(Fluids.WATER)
        return super.getStateForPlacement(context)
            ?.setValue(FACING, context.horizontalDirection)
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
        random: RandomSource
    ): BlockState {
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(
            pos,
            Fluids.WATER,
            Fluids.WATER.getTickDelay(level)
        )
        return super.updateShape(
            state,
            level,
            ticks,
            pos,
            directionToNeighbour,
            neighbourPos,
            neighbourState,
            random
        )
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false)
        else super.getFluidState(state)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return shapeFor(state.getValue(BILLETS), state.getValue(FACING))
    }

    override fun placeLiquid(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        fluidState: FluidState
    ): Boolean {
        if (!state.getValue(WATERLOGGED) && fluidState.`is`(WATER)) {
            val newState = state.setValue(WATERLOGGED, true)
            level.setBlock(pos, newState, 3)
            level.scheduleTick(pos, fluidState.type, fluidState.type.getTickDelay(level))
            return true
        }
        return false
    }

    public companion object {
        public val CODEC: MapCodec<FirewoodBlock> = simpleCodec(::FirewoodBlock)
        public val BILLETS: IntegerProperty =
            IntegerProperty.create("billets", MIN_BILLETS, MAX_BILLETS)
        public const val MIN_BILLETS: Int = 1
        public const val MAX_BILLETS: Int = 12
        public val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        // Placement order, drawn facing south: left → right, then back → front, per layer.
        private val BILLETS_IN_ORDER: List<VoxelShape> = listOf(
            box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
            box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
            box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
            box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
            box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
            box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
            box(11.0, 8.0, 0.0, 15.0, 12.0, 16.0),
            box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0),
            box(1.0, 8.0, 0.0, 5.0, 12.0, 16.0),
            box(0.0, 12.0, 11.0, 16.0, 16.0, 15.0),
            box(0.0, 12.0, 6.0, 16.0, 16.0, 10.0),
            box(0.0, 12.0, 1.0, 16.0, 16.0, 5.0),
        )

        private val SHAPES: List<Map<Direction, VoxelShape>> =
            (MIN_BILLETS..MAX_BILLETS).map { count ->
                val stack = BILLETS_IN_ORDER.take(count).reduce(Shapes::or)
                Shapes.rotateHorizontal(stack, OctahedralGroup.BLOCK_ROT_Y_180)
            }

        public fun shapeFor(billets: Int, facing: Direction): VoxelShape =
            SHAPES[billets - MIN_BILLETS].getValue(facing)

        @JvmStatic
        public fun maybeSplit(context: UseOnContext): Boolean {
            val level: Level = context.level
            val pos: BlockPos = context.clickedPos
            val player: Player = context.player ?: return false
            val state = level.getBlockState(pos)
            if (state.canBeSplit()) {
                if (context.clickedFace.axis === state.getValue(BlockStateProperties.AXIS)) {
                    if (level.isClientSide) return true
                    level.destroyBlock(pos, false, player, 512)
                    val firewood = ItemStack(OutsiderItems.FIREWOOD, 4)
                    popResource(level, pos, firewood)
                    val axe = context.itemInHand
                    axe.hurtAndBreak(1, player, context.hand)
                    return true
                }
            }
            return false
        }
    }
}