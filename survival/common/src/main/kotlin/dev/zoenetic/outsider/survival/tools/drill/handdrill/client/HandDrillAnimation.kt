package dev.zoenetic.outsider.survival.tools.drill.handdrill.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.animation.client.itemuse.FirstPersonGrip
import dev.zoenetic.outsider.survival.animation.client.itemuse.ItemDisplay
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation
import dev.zoenetic.outsider.survival.animation.client.itemuse.aimAt
import dev.zoenetic.outsider.survival.animation.client.itemuse.sign
import dev.zoenetic.outsider.survival.animation.client.itemuse.slideAlongLength
import dev.zoenetic.outsider.survival.animation.client.itemuse.undoFirstPersonItemArm
import dev.zoenetic.outsider.survival.animation.client.itemuse.undoThirdPersonHand
import dev.zoenetic.outsider.survival.fire.HearthBoard
import net.minecraft.client.model.HumanoidModel
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import kotlin.math.PI
import kotlin.math.sin

// client only
public class HandDrillAnimation : OutsiderItemUseAnimation {

    override val twoHanded: Boolean = true

    override fun firstPerson(
        poseStack: PoseStack,
        arm: HumanoidArm,
        inverseArmHeight: Float,
        ticks: Float,
    ) {
        val palm = RIGHT_GRIP.palm

        poseStack.translate(0F, palm.y() - FIRST_PERSON_DESCENT / 2F, palm.z())
        poseStack.mulPose(Axis.YP.rotation(spinAt(ticks)))
        poseStack.mulPose(STICK_UPRIGHT)
        ItemDisplay.HANDHELD_FIRST_PERSON.undoTranslation(poseStack, arm.sign)
        poseStack.undoFirstPersonItemArm(arm.sign, inverseArmHeight)
    }

    override fun firstPersonArm(poseStack: PoseStack, arm: HumanoidArm, ticks: Float) {
        val grip = if (arm == HumanoidArm.RIGHT) RIGHT_GRIP else LEFT_GRIP
        val stroke = arm.sign * sin(phaseAt(ticks)) * LIGHTING_STROKE_LENGTH
        val offset = Vector3f(grip.axis).mul(stroke)
            .sub(0F, descentAt(ticks) * FIRST_PERSON_DESCENT, 0F)
        grip.apply(poseStack, offset)
    }

    override fun thirdPerson(model: HumanoidModel<*>, ticks: Float) {
        val stroke = sin(phaseAt(ticks)) * PALM_STROKE
        val spindle = spindleTop(model)
        val gripY = spindle.y + descentAt(ticks) * GRIP_LENGTH

        model.rightArm.aimAt(spindle.x - PALM_HALF_GAP, gripY, spindle.z)
        model.leftArm.aimAt(spindle.x + PALM_HALF_GAP, gripY, spindle.z)
        model.rightArm.slideAlongLength(stroke)
        model.leftArm.slideAlongLength(-stroke)
    }

    override fun thirdPersonItem(
        poseStack: PoseStack,
        model: HumanoidModel<*>,
        arm: HumanoidArm,
        ticks: Float,
    ) {
        val spindle = spindleTop(model)

        poseStack.translate(spindle.x / 16F, (spindle.y + GRIP_LENGTH / 2F) / 16F, spindle.z / 16F)
        poseStack.mulPose(Axis.YP.rotation(-spinAt(ticks)))
        poseStack.mulPose(STICK_UPRIGHT_THIRD_PERSON)
        ItemDisplay.HANDHELD_THIRD_PERSON.undoTranslation(poseStack, arm.sign)
        poseStack.undoThirdPersonHand(arm.sign)
    }

    private fun spindleTop(model: HumanoidModel<*>): Vector3f = Vector3f(
        model.body.x,
        model.body.y + SHOULDER_Y + GRIP_TOP_BELOW_SHOULDER,
        model.body.z + SPINDLE_Z,
    )

    private fun descentAt(ticks: Float): Float = (ticks % DESCENT_TICKS) / DESCENT_TICKS

    private fun phaseAt(ticks: Float): Float =
        ticks * (2.0 * PI * LIGHTING_STROKES_PER_SECOND / 20.0).toFloat()

    private fun spinAt(ticks: Float): Float = sin(phaseAt(ticks)) * PALM_STROKE / SPINDLE_RADIUS

    public companion object {
        public const val LIGHTING_STROKES_PER_SECOND: Double = 3.0
        public const val LIGHTING_STROKE_LENGTH: Float = 0.08F

        public const val PALM_STROKE: Float = 1.5F
        public const val PALM_HALF_GAP: Float = 2.5F

        public const val SPINDLE_Z: Float = -HearthBoard.SPINDLE_PIXELS
        public const val SPINDLE_RADIUS: Float = 0.5F

        public const val GRIP_TOP_BELOW_SHOULDER: Float = 6F
        public const val GRIP_LENGTH: Float = 4F

        public const val DESCENT_TICKS: Float = 40F
        public const val FIRST_PERSON_DESCENT: Float = 0.15F

        // arm pivots sit this far below the body's
        private const val SHOULDER_Y = 2F

        public const val FIRST_PERSON_PALM_X: Float = 0.12F
        public const val SHOULDER_SPREAD: Float = 0.1F

        private val RIGHT_GRIP = FirstPersonGrip(1F, FIRST_PERSON_PALM_X, SHOULDER_SPREAD)
        private val LEFT_GRIP = FirstPersonGrip(-1F, FIRST_PERSON_PALM_X, SHOULDER_SPREAD)

        private val STICK_UPRIGHT: Quaternionfc = Quaternionf()
            .rotationZ(45F * Mth.DEG_TO_RAD)
            .mul(ItemDisplay.HANDHELD_FIRST_PERSON.inverseRotation(1F))

        private val STICK_UPRIGHT_THIRD_PERSON: Quaternionfc = Quaternionf()
            .rotationZ(-45F * Mth.DEG_TO_RAD)
            .rotateX(180F * Mth.DEG_TO_RAD)
            .mul(ItemDisplay.HANDHELD_THIRD_PERSON.inverseRotation(1F))
    }
}
