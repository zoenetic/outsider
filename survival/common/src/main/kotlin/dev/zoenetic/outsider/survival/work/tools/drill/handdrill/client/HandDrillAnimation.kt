package dev.zoenetic.outsider.survival.work.tools.drill.handdrill.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.entity.HumanoidArm
import kotlin.math.PI
import kotlin.math.sin

// client only
public class HandDrillAnimation : OutsiderItemUseAnimation {

    override fun firstPerson(
        poseStack: PoseStack,
        arm: HumanoidArm,
        inverseArmHeight: Float,
        ticks: Float,
    ) {
        val sign = if (arm == HumanoidArm.RIGHT) 1F else -1F
        val stroke = sin(phaseAt(ticks)) * LIGHTING_STROKE_LENGTH

        val anchorX = sign * 0.64F
        val anchorY = -0.6F + inverseArmHeight * -0.6F
        val anchorZ = -0.72F

        val targetX = sign * 0.35F
        val targetY = -0.35F
        val targetZ = -0.72F + sign * stroke

        poseStack.translate(targetX, targetY, targetZ)
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * 55F))
        poseStack.translate(-anchorX, -anchorY, -anchorZ)
    }

    override fun thirdPerson(rightArm: ModelPart, leftArm: ModelPart, ticks: Float) {
        val slide = sin(phaseAt(ticks)) * LIGHTING_ARM_SLIDE
        rightArm.xRot = -1.3F
        rightArm.yRot = -0.55F
        rightArm.zRot = 0F
        rightArm.z = slide

        leftArm.xRot = -1.3F
        leftArm.yRot = 0.55F
        leftArm.zRot = 0F
        leftArm.z = -slide
    }

    private fun phaseAt(ticks: Float): Float =
        ticks * (2.0 * PI * LIGHTING_STROKES_PER_SECOND / 20.0).toFloat()

    public companion object {
        public const val LIGHTING_STROKES_PER_SECOND: Double = 3.0
        public const val LIGHTING_STROKE_LENGTH: Float = 0.08F
        public const val LIGHTING_ARM_SLIDE: Float = 2.0F
    }
}
