package dev.zoenetic.outsider.survival.work.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.zoenetic.outsider.survival.animation.client.player.KNEE_FRONT
import dev.zoenetic.outsider.survival.animation.client.player.KNEE_Y
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimation
import dev.zoenetic.outsider.survival.animation.client.player.shinOf
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.hypot

// client only
public class KneelingAnimation : OutsiderPlayerAnimation {
    override fun bodyRotation(strength: Float, state: LivingEntityRenderState): Float = 0F
    override fun firstPerson(strength: Float, poseStack: PoseStack, ticks: Float) {}
    override fun thirdPerson(strength: Float, model: HumanoidModel<*>, state: HumanoidRenderState) {
        val drop: Float = KNEEL_DROP * strength
        val crouchDrop: Float = if (state.isCrouching) CROUCH_RENDER_DROP else 0F
        val partDrop: Float = drop - crouchDrop
        val hipHeight: Float = LEG_LENGTH - drop

        model.head.y = partDrop
        model.body.y = partDrop
        model.body.xRot = 0F

        model.leftArm.y = ARM_PIVOT_Y + partDrop
        model.rightArm.y = ARM_PIVOT_Y + partDrop

        val trailingThigh: Float = thighAngle(hipHeight)
        kneel(
            model.leftLeg,
            partDrop,
            trailingThigh,
            LYING_FLAT * strength,
            SHIN_SLIDE * strength,
        )
        val frontThigh: Float = thighAngle(hipHeight - SHIN_LENGTH)
        kneel(model.rightLeg, partDrop, frontThigh, 0F, 0F)
    }

    private fun kneel(leg: ModelPart, partDrop: Float, thigh: Float, shin: Float, slide: Float) {
        leg.y = LEG_PIVOT_Y + partDrop
        leg.z = 0F
        leg.xRot = thigh
        shinOf(leg)?.let {
            it.xRot = shin - thigh
            it.z += slide
        }
    }

    private fun thighAngle(kneeHeight: Float): Float =
        KNEE_LEAN - acos((kneeHeight / KNEE_REACH).coerceIn(-1F, 1F))

    private companion object {
        const val KNEEL_DROP: Float = 6F
        val KNEE_REACH: Float = hypot(KNEE_Y, KNEE_FRONT)
        val KNEE_LEAN: Float = atan2(-KNEE_FRONT, KNEE_Y)
        const val LEG_LENGTH: Float = 12F
        const val SHIN_LENGTH: Float = LEG_LENGTH - KNEE_Y
        const val SHIN_SLIDE: Float = 2F
        const val ARM_PIVOT_Y: Float = 2F
        const val LEG_PIVOT_Y: Float = 12F
        const val CROUCH_RENDER_DROP: Float = 2F
        const val LYING_FLAT: Float = (Math.PI / 2).toFloat()
    }
}
