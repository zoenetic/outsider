package dev.zoenetic.outsider.survival.work.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimation
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

// client only
public class KneelingAnimation : OutsiderPlayerAnimation {
    override fun bodyRotation(strength: Float, state: LivingEntityRenderState): Float = 0F
    override fun firstPerson(strength: Float, poseStack: PoseStack, ticks: Float) {}
    override fun thirdPerson(strength: Float, model: HumanoidModel<*>, state: HumanoidRenderState) {
        val drop = KNEEL_DROP * strength
        val crouchDrop = if (state.isCrouching) CROUCH_RENDER_DROP else 0F
        val partDrop = drop - crouchDrop
        val ground = LEG_PIVOT_Y + LEG_LENGTH - crouchDrop

        model.head.y = partDrop
        model.body.y = partDrop
        model.body.xRot = 0F

        model.leftArm.y = ARM_PIVOT_Y + partDrop
        model.rightArm.y = ARM_PIVOT_Y + partDrop

        model.leftLeg.y = LEG_PIVOT_Y + partDrop
        model.leftLeg.z = 0F
        model.leftLeg.xRot = acos((LEG_LENGTH - drop) / LEG_LENGTH)

        val shinTilt = FRONT_SHIN_TILT * strength
        val footZ = FRONT_FOOT_Z * strength
        model.rightLeg.y = ground - LEG_LENGTH * cos(shinTilt)
        model.rightLeg.z = footZ + LEG_LENGTH * sin(shinTilt)
        model.rightLeg.xRot = -shinTilt
    }

    private companion object {
        const val KNEEL_DROP = 6F
        const val LEG_LENGTH = 12F
        const val ARM_PIVOT_Y = 2F
        const val LEG_PIVOT_Y = 12F
        const val CROUCH_RENDER_DROP = 2F
        const val FRONT_SHIN_TILT = 0.2F
        const val FRONT_FOOT_Z = -7F
    }
}
