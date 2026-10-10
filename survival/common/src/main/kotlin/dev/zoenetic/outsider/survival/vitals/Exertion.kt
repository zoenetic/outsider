package dev.zoenetic.outsider.survival.vitals

import dev.zoenetic.outsider.survival.Survival
import net.minecraft.SharedConstants
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.ItemTags
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.animal.equine.AbstractHorse
import net.minecraft.world.entity.animal.happyghast.HappyGhast
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus
import net.minecraft.world.entity.animal.pig.Pig
import net.minecraft.world.entity.monster.Strider
import net.minecraft.world.entity.vehicle.boat.AbstractBoat
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.item.equipment.EquipmentAssets
import net.minecraft.world.level.Level
import java.util.*
import kotlin.math.abs

internal val SLEEPING: MET = MET(0.9)
internal val DRINKING: MET = MET(1.3)
internal val RIDING: MET = MET(1.3)
internal val EATING: MET = MET(1.5)
internal val CROUCHING: MET = MET(2.0)
internal val FLYING: MET = MET(2.0)
internal val PLACING_BLOCKS: MET = MET(3.0)
internal val BLOCKING: MET = MET(3.0)
internal val CRAWLING: MET = MET(4.0)
internal val DRAWING_BOW: MET = MET(4.0)
internal val TREADING_WATER: MET = MET(4.0)
internal val WALKING: MET = MET(4.0)
internal val SHIVERING: MET = MET(4.5)
internal val ROWING: MET = MET(6.0)
internal val USING_HOE: MET = MET(6.0)
internal val USING_SHOVEL: MET = MET(7.0)
internal val SWINGING_AXE: MET = MET(7.5)
internal val CLIMBING: MET = MET(8.0)
internal val JUMPING: MET = MET(8.0)
internal val SWINGING_PICKAXE: MET = MET(8.0)
internal val SWIMMING: MET = MET(8.0)
internal val SWINGING_SWORD: MET = MET(9.0)
internal val SWINGING_MACE: MET = MET(10.0)
internal val JABBING_SPEAR: MET = MET(10.0)
internal val JABBING_TRIDENT: MET = MET(12.0)
internal val SPRINTING: MET = MET(14.0)
internal val CHARGING_TRIDENT: MET = MET(4.0)
internal val BRACING_SPEAR: MET = MET(2.0)

internal const val LEATHER_WEIGHT = 0.7
internal const val DIAMOND_WEIGHT = 1.0
internal const val CHAINMAIL_WEIGHT = 1.2
internal const val IRON_WEIGHT = 2.5
internal const val COPPER_WEIGHT = 2.8
internal const val NETHERITE_WEIGHT = 3.5
internal const val GOLD_WEIGHT = 6.0
internal const val TURTLE_SCUTE_WEIGHT = 1.33

internal const val HELMET_FRACTION = 0.15
internal const val CHESTPLATE_FRACTION = 0.40
internal const val LEGGINGS_FRACTION = 0.30
internal const val BOOTS_FRACTION = 0.15

internal const val SHIELD_WEIGHT = 0.50

public class Exertion(private val accumulator: ArrayDeque<MET>) {

    internal fun trim() {
        while (accumulator.size > SharedConstants.TICKS_PER_SECOND) accumulator.removeFirst()
    }

    public companion object {

        internal val cache: HashMap<UUID, Exertion> = hashMapOf()

        internal fun average(uuid: UUID): MET? {
            val accumulator = cache[uuid]?.accumulator ?: return null
            return accumulator.average()
        }

        // TODO: Rest of inventory?
        // TODO: Pull this out into its own system that body temp can pull from too
        // TODO: Once isolated, implement caching
        internal val ServerPlayer.encumbrance: List<MET>
            get() {
                val factors = mutableListOf<MET>()
                for (slot in EquipmentSlotGroup.ARMOR) {
                    val stack = getItemBySlot(slot)
                    val material = stack.get(DataComponents.EQUIPPABLE)?.assetId?.orElse(null)
                    val weight = when (material) {
                        EquipmentAssets.TURTLE_SCUTE -> TURTLE_SCUTE_WEIGHT
                        EquipmentAssets.LEATHER -> LEATHER_WEIGHT
                        EquipmentAssets.DIAMOND -> DIAMOND_WEIGHT
                        EquipmentAssets.CHAINMAIL -> CHAINMAIL_WEIGHT
                        EquipmentAssets.IRON -> IRON_WEIGHT
                        EquipmentAssets.COPPER -> COPPER_WEIGHT
                        EquipmentAssets.NETHERITE -> NETHERITE_WEIGHT
                        EquipmentAssets.GOLD -> GOLD_WEIGHT
                        else -> 0.0
                    }
                    val fraction = when (slot) {
                        EquipmentSlot.HEAD -> HELMET_FRACTION
                        EquipmentSlot.CHEST -> CHESTPLATE_FRACTION
                        EquipmentSlot.LEGS -> LEGGINGS_FRACTION
                        EquipmentSlot.FEET -> BOOTS_FRACTION
                        else -> 0.0
                    }
                    factors.add(MET(weight * fraction))
                }
                if (offhandItem.has(DataComponents.BLOCKS_ATTACKS)) factors.add(MET(SHIELD_WEIGHT))
                return factors
            }

        internal val ServerPlayer.isClimbing: Boolean
            get() = onClimbable() && abs(knownMovement.y) > Mth.EPSILON

        internal val ServerPlayer.isPlacingBlocks: Boolean
            get() {
                if (!swinging) return false
                return getItemInHand(swingingArm ?: InteractionHand.MAIN_HAND).item is BlockItem
            }

        internal val ServerPlayer.isAscending: Boolean
            get() {
                if (onGround() || isPassenger || isFallFlying) return false
                if (isInWater || onClimbable()) return false
                return knownMovement.y > Mth.EPSILON
            }

        internal val ServerPlayer.isRiding: Boolean
            get() {
                return when (this.vehicle) {
                    is AbstractHorse,
                    is AbstractNautilus,
                    is HappyGhast,
                    is Pig,
                    is Strider,
                    -> true

                    else -> false
                }
            }

        internal val ServerPlayer.isRowing: Boolean
            get() {
                val boat = controlledVehicle as? AbstractBoat ?: return false
                return boat.getPaddleState(0) || boat.getPaddleState(1)
            }

        internal val ServerPlayer.isShivering: Boolean
            get() {
                val vitals = Survival.platform.vitals.get(this) ?: return false
                return shiverIntensity(vitals.bodyTemperature.heat) > 0.0
            }

        internal val ServerPlayer.isWalking: Boolean
            get() {
                val speed = knownMovement.horizontalDistance()
                return speed > Mth.EPSILON && !isSprinting && !isFallFlying && !isPassenger
            }

        internal val ServerPlayer.isTreadingWater: Boolean
            get() {
                return isInWater && !onGround() && !isSwimming && !isPassenger
            }

        internal fun useActions(player: ServerPlayer): List<MET> {
            val activities = mutableListOf<MET>()
            if (player.isUsingItem) {
                val stack = player.getUseItem()
                val animation = stack.useAnimation
                when (animation) {
                    ItemUseAnimation.BOW -> activities.add(DRAWING_BOW)
                    ItemUseAnimation.CROSSBOW -> activities.add(DRAWING_BOW)
                    ItemUseAnimation.SPEAR -> activities.add(BRACING_SPEAR)
                    ItemUseAnimation.TRIDENT -> activities.add(CHARGING_TRIDENT)
                    ItemUseAnimation.EAT -> activities.add(EATING)
                    ItemUseAnimation.DRINK -> activities.add(DRINKING)
                    else -> {}
                }
            }
            if (player.swinging && player.swingingArm == InteractionHand.MAIN_HAND) {
                val item = player.mainHandItem
                when {
                    item.`is`(ItemTags.HOES) -> activities.add(USING_HOE)
                    item.`is`(ItemTags.SHOVELS) -> activities.add(USING_SHOVEL)
                    item.`is`(ItemTags.AXES) -> activities.add(SWINGING_AXE)
                    item.`is`(ItemTags.PICKAXES) -> activities.add(SWINGING_PICKAXE)
                    item.`is`(ItemTags.SWORDS) -> activities.add(SWINGING_SWORD)
                    item.`is`(ItemTags.MACE_ENCHANTABLE) -> activities.add(SWINGING_MACE)
                    item.`is`(ItemTags.SPEARS) -> activities.add(JABBING_SPEAR)
                    item.`is`(ItemTags.TRIDENT_ENCHANTABLE) -> activities.add(JABBING_TRIDENT)
                }
            }
            return activities
        }

        public fun calculateMET(player: ServerPlayer): MET {
            val activities: MutableList<MET> = mutableListOf()
            if (player.isAscending) activities.add(JUMPING)
            if (player.isBlocking) activities.add(BLOCKING)
            if (player.isClimbing) activities.add(CLIMBING)
            if (player.isCrouching) activities.add(CROUCHING)
            if (player.isFallFlying) activities.add(FLYING)
            if (player.isPlacingBlocks) activities.add(PLACING_BLOCKS)
            if (player.isRiding) activities.add(RIDING)
            if (player.isRowing) activities.add(ROWING)
            if (player.isShivering) activities.add(SHIVERING)
            if (player.isSleeping) activities.add(SLEEPING)
            if (player.isSprinting) activities.add(SPRINTING)
            if (player.isSwimming) activities.add(SWIMMING)
            if (player.isTreadingWater) activities.add(TREADING_WATER)
            if (player.isVisuallyCrawling) activities.add(CRAWLING)
            if (player.isWalking) activities.add(WALKING)
            activities.addAll(useActions(player))

            val exertion = activities.sumOf { it.value - 1.0 }
            val load = player.encumbrance.sumOf { it.value }
            return MET(1.0 + exertion + load)
        }

        public fun remove(uuid: UUID) {
            cache.remove(uuid)
        }

        public fun tick(level: Level) {
            if (level !is ServerLevel) return
            val players = level.players()
            for (player in players) {
                tick(player)
            }
        }

        public fun tick(player: ServerPlayer) {
            val exertion = cache.getOrPut(player.uuid) {
                Exertion(
                    ArrayDeque<MET>(),
                )
            }
            val met = calculateMET(player)
            exertion.accumulator.add(met)
            exertion.trim()
        }

        public val DEFAULT: MET = MET(1.0)
    }
}
