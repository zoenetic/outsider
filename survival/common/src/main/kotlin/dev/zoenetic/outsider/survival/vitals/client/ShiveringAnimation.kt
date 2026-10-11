package dev.zoenetic.outsider.survival.vitals.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimation
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import kotlin.math.sin

// client only
public class ShiveringAnimation : OutsiderPlayerAnimation {

    override fun bodyRotation(strength: Float, state: LivingEntityRenderState): Float =
        (strength * SHIVER_AMPLITUDE_DEGREES * sin(state.ageInTicks * SHIVER_FREQUENCY)).toFloat()

    override fun firstPerson(strength: Float, poseStack: PoseStack, ticks: Float) {
        val phase = ticks * SHIVER_FREQUENCY
        val roll = sin(phase) * SHIVER_AMPLITUDE_DEGREES * strength
        val yaw =
            sin(phase * SHIVER_YAW_RATIO) * SHIVER_AMPLITUDE_DEGREES * SHIVER_YAW_SCALE * strength
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll.toFloat()))
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw.toFloat()))
    }

    public companion object {
        public const val SHIVER_AMPLITUDE_DEGREES: Double = 1.5
        public const val SHIVER_FREQUENCY: Double = 2.0
        private const val SHIVER_YAW_RATIO = 1.37
        private const val SHIVER_YAW_SCALE = 0.25
    }
}
