package dev.zoenetic.outsider.survival.torch

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseTorchBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

public open class OutsiderDeadTorchBlock(properties: Properties) : BaseTorchBlock(properties) {
    init {
        registerDefaultState(
            stateDefinition.any(),
        )
    }

    override fun codec(): MapCodec<out BaseTorchBlock> = CODEC

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape = SHAPE

    override fun useItemOn(
        itemStack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult,
    ): InteractionResult {
        if (itemStack.`is`(Items.FLINT_AND_STEEL) && itemStack.damageValue < itemStack.maxDamage) {
            level.playSound(
                null,
                pos,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1F,
                1F,
            )
            return InteractionResult.FAIL
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult)
    }

    public companion object {
        public val CODEC: MapCodec<OutsiderDeadTorchBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<OutsiderDeadTorchBlock> ->
                i.group(
                    Properties.CODEC.fieldOf("properties").forGetter { b -> b.properties },
                ).apply(i, ::OutsiderDeadTorchBlock)
            }

        internal val SHAPE: VoxelShape = column(4.0, 0.0, 8.0)
    }
}
