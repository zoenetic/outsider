package dev.zoenetic.outsider.survival.campfire

import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult

public class OutsiderDeadCampfireBlock(properties: Properties) : Block(properties),
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

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (itemStack.`is`(Items.FLINT_AND_STEEL) && itemStack.damageValue < itemStack.maxDamage) {
            level.playSound(
                null,
                pos,
                OutsiderSounds.FLINT_AND_STEEL_FAIL,
                SoundSource.BLOCKS,
                1F,
                1F
            )
            return InteractionResult.FAIL
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult)
    }

    public companion object {
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
        public val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
    }
}