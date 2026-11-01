package dev.zoenetic.outsider.survival.fire.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.animation.client.itemuse.FirstPersonGrip
import dev.zoenetic.outsider.survival.animation.client.itemuse.ItemDisplay
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation
import dev.zoenetic.outsider.survival.animation.client.itemuse.aimAt
import dev.zoenetic.outsider.survival.animation.client.itemuse.sign
import dev.zoenetic.outsider.survival.animation.client.itemuse.undoFirstPersonItemArm
import dev.zoenetic.outsider.survival.animation.client.itemuse.undoThirdPersonHand
import net.minecraft.client.model.HumanoidModel
import net.minecraft.world.entity.HumanoidArm
import org.joml.Vector3f

public class BlowingAnimation : OutsiderItemUseAnimation {

    override val twoHanded: Boolean = true

    override fun firstPerson(
        poseStack: PoseStack,
        arm: HumanoidArm,
        inverseArmHeight: Float,
        ticks: Float,
    ) {
        val palm = RIGHT_GRIP.palm
        poseStack.translate(0F, palm.y(), palm.z())
        poseStack.scale(
            FIRST_PERSON_TINDER_SCALE,
            FIRST_PERSON_TINDER_SCALE,
            FIRST_PERSON_TINDER_SCALE,
        )
        poseStack.mulPose(ItemDisplay.GENERATED_FIRST_PERSON.inverseRotation(arm.sign))
        ItemDisplay.GENERATED_FIRST_PERSON.undoTranslation(poseStack, arm.sign)
        poseStack.undoFirstPersonItemArm(arm.sign, inverseArmHeight)
    }

    override fun firstPersonArm(poseStack: PoseStack, arm: HumanoidArm, ticks: Float) {
        (if (arm == HumanoidArm.RIGHT) RIGHT_GRIP else LEFT_GRIP).apply(poseStack)
    }

    override fun thirdPerson(model: HumanoidModel<*>, ticks: Float) {
        val cup = cupCentre(model)
        model.rightArm.aimAt(cup.x - PALM_HALF_GAP, cup.y, cup.z)
        model.leftArm.aimAt(cup.x + PALM_HALF_GAP, cup.y, cup.z)
    }

    override fun thirdPersonItem(
        poseStack: PoseStack,
        model: HumanoidModel<*>,
        arm: HumanoidArm,
        ticks: Float,
    ) {
        val cup = cupCentre(model)
        poseStack.translate(cup.x / 16F, cup.y / 16F, cup.z / 16F)
        poseStack.mulPose(Axis.ZP.rotationDegrees(180F))
        poseStack.mulPose(ItemDisplay.GENERATED_FIRST_PERSON.inverseRotation(arm.sign))
        ItemDisplay.GENERATED_FIRST_PERSON.undoTranslation(poseStack, arm.sign)
        poseStack.undoThirdPersonHand(arm.sign)
    }

    private fun cupCentre(model: HumanoidModel<*>): Vector3f = Vector3f(
        model.head.x,
        model.head.y + MOUTH_Y,
        model.head.z + CUP_Z,
    )

    private companion object {
        const val MOUTH_Y = -2F
        const val CUP_Z = -7F
        const val PALM_HALF_GAP = 2F
        const val FIRST_PERSON_TINDER_SCALE = 0.6F

        val RIGHT_GRIP =
            FirstPersonGrip(1F, palmX = 0.1F, shoulderSpread = 0.05F, lift = 0.2F, reach = -0.3F)
        val LEFT_GRIP =
            FirstPersonGrip(-1F, palmX = 0.1F, shoulderSpread = 0.05F, lift = 0.2F, reach = -0.3F)
    }
}
