package dev.zoenetic.unbidden.survival.campfire

import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty

public class UnbiddenDeadCampfireBlock(properties: Properties) : Block(properties),
    SimpleWaterloggedBlock {
    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(FACING, Direction.NORTH)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, FACING)
    }

    public companion object {
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
        public val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
    }
}