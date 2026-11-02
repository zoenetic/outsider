package dev.zoenetic.outsider.survival.tools

import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockStateProperties.FUEL_LEVEL
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlockTags
import dev.zoenetic.outsider.survival.registry.items.OutsiderComponents
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Unit
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.Interaction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

public class TinderItem(properties: Properties) :
    Item(properties),
    WorkingToolItem {

    override fun getUseDuration(itemStack: ItemStack, user: LivingEntity): Int = 72000

    override fun inventoryTick(
        itemStack: ItemStack,
        level: ServerLevel,
        owner: Entity,
        slot: EquipmentSlot?,
    ) {
        if (!itemStack.has(OutsiderComponents.LIT)) return
        val burnsOutAt = itemStack.get(OutsiderComponents.BURNS_OUT_AT) ?: Time(0)
        if (Time(level.gameTime) >= burnsOutAt) {
            burnOut(itemStack)
            smokeOut(level, owner, slot)
            level.playSound(
                null,
                owner.x,
                owner.y,
                owner.z,
                OutsiderSounds.TINDER_BURNS_UP,
                SoundSource.PLAYERS,
            )
            return
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult =
        OutsiderWork.LIGHTING_FIRE.use(player, hand, this)

    override fun useOn(context: UseOnContext): InteractionResult {
        if (!context.itemInHand.has(OutsiderComponents.LIT)) return InteractionResult.PASS
        val lit = lightBlock(context)
        if (lit != InteractionResult.PASS) return lit
        return setAlight(context)
    }

    private fun lightBlock(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos
        val state = level.getBlockState(pos)
        if (!state.canBeLit()) return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.SUCCESS
        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), Block.UPDATE_ALL)
        level.playSound(null, pos, OutsiderSounds.FIRE_SUCCESS, SoundSource.BLOCKS)
        burnOut(context.itemInHand)
        return InteractionResult.SUCCESS
    }

    private fun BlockState.canBeLit(): Boolean = `is`(OutsiderBlockTags.LIGHTABLE_WITH_FIRE) &&
        !getValueOrElse(BlockStateProperties.LIT, true) &&
        !getValueOrElse(BlockStateProperties.WATERLOGGED, false) &&
        getValueOrElse(FUEL_LEVEL, 1) != 0

    private fun setAlight(context: UseOnContext): InteractionResult {
        val level = context.level
        val firePos = context.clickedPos.relative(context.clickedFace)
        if (!BaseFireBlock.canBePlacedAt(level, firePos, context.horizontalDirection)) {
            return InteractionResult.PASS
        }
        if (level.isClientSide) return InteractionResult.SUCCESS
        level.setBlock(firePos, BaseFireBlock.getState(level, firePos), Block.UPDATE_ALL)
        level.playSound(null, firePos, OutsiderSounds.FIRE_SUCCESS, SoundSource.BLOCKS)
        level.gameEvent(context.player, GameEvent.BLOCK_PLACE, firePos)
        burnOut(context.itemInHand)
        return InteractionResult.SUCCESS
    }
    
    private fun smokeOut(level: ServerLevel, owner: Entity, slot: EquipmentSlot?) {
        val hand: InteractionHand? = when (slot) {
            EquipmentSlot.MAINHAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFFHAND -> InteractionHand.OFF_HAND
            else -> null
        }
        val at: Vec3 = if (owner is Player && hand != null) {
            handPosition(owner, hand)
        } else {
            owner.position().add(0.0, owner.bbHeight / 2.0, 0.0)
        }
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, BURN_OUT_SMOKE, 0.05, 0.05, 0.05, 0.01)
    }

    public companion object {    
        public const val FLAME_CHANCE: Float = 0.2F

        private const val BURN_OUT_SMOKE: Int = 8
        private const val HAND_SPREAD: Double = 0.35
        private const val HAND_REACH: Double = 0.3
        private const val HAND_HEIGHT: Double = 0.8
        private const val CROUCHED_HAND_HEIGHT: Double = 0.55

        public fun burnOut(itemStack: ItemStack) {
            itemStack.shrink(1)
            itemStack.remove(OutsiderComponents.LIT)
            itemStack.remove(OutsiderComponents.BURNS_OUT_AT)
        }

        public fun handPosition(player: Player, hand: InteractionHand): Vec3 {
            val rightHand =
                (hand == InteractionHand.MAIN_HAND) == (player.mainArm == HumanoidArm.RIGHT)
            val yaw = Math.toRadians(player.yBodyRot.toDouble())
            val right = Vec3(
                -cos(yaw),
                0.0,
                -sin(yaw),
            ).scale(if (rightHand) HAND_SPREAD else -HAND_SPREAD)
            val forward = Vec3(-sin(yaw), 0.0, cos(yaw)).scale(HAND_REACH)
            val height = if (player.isCrouching) CROUCHED_HAND_HEIGHT else HAND_HEIGHT
            return player.position().add(right).add(forward).add(0.0, height, 0.0)
        }

        public val BURN_TIME: Duration = Duration(200)

        public fun light(itemStack: ItemStack, now: Time) {
            itemStack.set(OutsiderComponents.LIT, Unit.INSTANCE)
            itemStack.set(OutsiderComponents.BURNS_OUT_AT, now + BURN_TIME)
        }

        public fun ItemStack.isLitTinder(): Boolean =
            item is TinderItem && has(OutsiderComponents.LIT)

        public fun Player.holdsLitTinder(): Boolean = InteractionHand.entries.any {
            getItemInHand(it).isLitTinder()
        }
    }
}
