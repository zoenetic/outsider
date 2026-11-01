package dev.zoenetic.outsider.survival.animation.client.itemuse

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// client only

internal val HumanoidArm.sign: Float get() = if (this == HumanoidArm.RIGHT) 1F else -1F

internal fun ModelPart.aimAt(targetX: Float, targetY: Float, targetZ: Float) {
    val dx = targetX - x
    val dy = targetY - y
    val dz = targetZ - z
    val length = sqrt(dx * dx + dy * dy + dz * dz)
    xRot = -acos((dy / length).coerceIn(-1F, 1F))
    yRot = atan2(-dx, -dz)
    zRot = 0F
}

internal fun ModelPart.slideAlongLength(distance: Float) {
    x += sin(xRot) * sin(yRot) * distance
    y += cos(xRot) * distance
    z += sin(xRot) * cos(yRot) * distance
}

internal fun PoseStack.undoFirstPersonItemArm(sign: Float, inverseArmHeight: Float) {
    translate(-sign * 0.56F, 0.52F + inverseArmHeight * 0.6F, 0.72F)
}

internal fun PoseStack.undoThirdPersonHand(sign: Float) {
    translate(-sign / 16F, -2F / 16F, 10F / 16F)
    mulPose(Axis.YP.rotationDegrees(180F))
    mulPose(Axis.XP.rotationDegrees(90F))
}

internal class ItemDisplay(
    private val rotation: Vector3fc,
    private val translation: Vector3fc,
    private val mirrorsRotation: Boolean,
) {
    fun inverseRotation(sign: Float): Quaternionfc {
        val mirror = if (mirrorsRotation) sign else 1F
        return Quaternionf().rotationXYZ(
            rotation.x() * Mth.DEG_TO_RAD,
            mirror * rotation.y() * Mth.DEG_TO_RAD,
            mirror * rotation.z() * Mth.DEG_TO_RAD,
        ).conjugate()
    }

    fun undoTranslation(poseStack: PoseStack, sign: Float) {
        poseStack.translate(
            -sign * translation.x() / 16F,
            -translation.y() / 16F,
            -translation.z() / 16F,
        )
    }

    companion object {
        val HANDHELD_FIRST_PERSON =
            ItemDisplay(Vector3f(0F, -90F, 25F), Vector3f(1.13F, 3.2F, 1.13F), false)
        val HANDHELD_THIRD_PERSON =
            ItemDisplay(Vector3f(0F, -90F, 55F), Vector3f(0F, 4F, 0.5F), false)
        val GENERATED_FIRST_PERSON =
            ItemDisplay(Vector3f(0F, -90F, 25F), Vector3f(1.13F, 3.2F, 1.13F), true)
        val GENERATED_THIRD_PERSON =
            ItemDisplay(Vector3f(0F, 0F, 0F), Vector3f(0F, 3F, 1F), true)
    }
}

internal class FirstPersonGrip(
    sign: Float,
    palmX: Float,
    shoulderSpread: Float,
    lift: Float = 0F,
    reach: Float = 0F,
) {
    private val vanilla = Matrix4f()
        .translate(sign * 0.64F, -0.6F, -0.72F)
        .rotateY(sign * 45F * Mth.DEG_TO_RAD)
        .translate(-sign, 3.6F, 3.5F)
        .rotateZ(sign * 120F * Mth.DEG_TO_RAD)
        .rotateX(200F * Mth.DEG_TO_RAD)
        .rotateY(sign * -135F * Mth.DEG_TO_RAD)
        .translate(sign * 5.6F, 0F, 0F)
        .translate(sign * -5F / 16F, 2F / 16F, 0F)
        .rotateZ(sign * 0.1F)

    val vanillaShoulder: Vector3fc =
        vanilla.transformPosition(Vector3f(-sign / 16F, -2F / 16F, 0F))
    private val vanillaHand: Vector3fc =
        vanilla.transformPosition(Vector3f(-sign / 16F, 10F / 16F, 0F))

    val shoulder: Vector3fc = Vector3f(vanillaShoulder).add(sign * shoulderSpread, 0F, 0F)
    val palm: Vector3fc =
        Vector3f(sign * palmX, vanillaHand.y() + lift, vanillaHand.z() - reach)
    val axis: Vector3fc = Vector3f(palm).sub(shoulder).normalize()
    val rotation: Quaternionfc = Quaternionf()
        .rotationTo(Vector3f(vanillaHand).sub(vanillaShoulder).normalize(), axis)

    fun apply(poseStack: PoseStack, offset: Vector3fc = ZERO) {
        poseStack.translate(offset.x(), offset.y(), offset.z())
        poseStack.translate(shoulder.x(), shoulder.y(), shoulder.z())
        poseStack.mulPose(rotation)
        poseStack.translate(-vanillaShoulder.x(), -vanillaShoulder.y(), -vanillaShoulder.z())
    }

    private companion object {
        val ZERO: Vector3fc = Vector3f()
    }
}
