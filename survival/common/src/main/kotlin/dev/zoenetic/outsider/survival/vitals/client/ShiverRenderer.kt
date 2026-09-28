package dev.zoenetic.outsider.survival.vitals.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.vitals.shiverIntensity
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.sin

public const val SHIVER_AMPLITUDE_DEGREES: Double = 1.5
public const val SHIVER_FREQUENCY: Double = 2.0

private const val SHIVER_YAW_RATIO = 1.37
private const val SHIVER_YAW_SCALE = 0.25

public interface ShiverState {
    public fun `outsider_survival$getShiver`(): Double
    public fun `outsider_survival$setShiver`(shiver: Double)
}

// client only
public object ShiverRenderer {

    public fun capture(entity: LivingEntity, state: LivingEntityRenderState) {
        val intensity = if (entity is Player) intensityOf(entity) else 0.0
        (state as ShiverState).`outsider_survival$setShiver`(intensity)
    }

    public fun applyToBodyRotation(bodyRot: Float, state: LivingEntityRenderState): Float {
        val intensity = (state as ShiverState).`outsider_survival$getShiver`()
        if (intensity <= 0.0) return bodyRot
        val wobble =
            intensity * SHIVER_AMPLITUDE_DEGREES * sin(state.ageInTicks * SHIVER_FREQUENCY)
        return bodyRot + wobble.toFloat()
    }

    public fun applyToHeldItems(player: LocalPlayer, frameInterp: Float, poseStack: PoseStack) {
        val intensity = intensityOf(player)
        if (intensity <= 0.0) return
        val phase = (player.tickCount + frameInterp) * SHIVER_FREQUENCY
        val roll = sin(phase) * SHIVER_AMPLITUDE_DEGREES * intensity
        val yaw =
            sin(phase * SHIVER_YAW_RATIO) * SHIVER_AMPLITUDE_DEGREES * SHIVER_YAW_SCALE * intensity
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll.toFloat()))
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw.toFloat()))
    }

    private fun intensityOf(player: Player): Double {
        val vitals = Survival.platform.vitals.get(player) ?: return 0.0
        return shiverIntensity(vitals.bodyTemperature.heat)
    }
}
