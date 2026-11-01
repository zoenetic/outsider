package dev.zoenetic.outsider.survival.drill.handdrill.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.tools.drill.handdrill.client.HandDrillAnimation
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.util.Mth
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HandDrillAnimationTests {

    private val animation = HandDrillAnimation()
    private val ticks = (0 until 60).map { it * 0.41F }

    // Three strokes a second, so the stroke repeats every 20 / 3 ticks.
    private val period = 20F / 3F

    // Kneeling pivots, in model space: the kneel drops the body and arms by 4 px under crouch.
    private val body = Vector3f(0F, 4F, 0F)
    private val rightShoulder = Vector3f(-5F, 6F, 0F)
    private val leftShoulder = Vector3f(5F, 6F, 0F)

    private fun kneelingModel(): HumanoidModel<HumanoidRenderState> {
        val mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0F)
        return HumanoidModel<HumanoidRenderState>(LayerDefinition.create(mesh, 64, 64).bakeRoot()).apply {
            this.body.at(this@HandDrillAnimationTests.body)
            rightArm.at(rightShoulder)
            leftArm.at(leftShoulder)
        }
    }

    private fun arms(ticks: Float): Pair<ModelPart, ModelPart> {
        val model = kneelingModel()
        animation.thirdPerson(model, ticks)
        return model.rightArm to model.leftArm
    }

    // What ItemInHandLayer applies after the skipped translateToHand: its offset from the hand,
    // then item/handheld's third-person display (both hands end up with the same rotation).
    private fun thirdPersonStick(arm: HumanoidArm, ticks: Float): Matrix4f {
        val sign = if (arm == HumanoidArm.RIGHT) 1F else -1F
        val pose = PoseStack()
        animation.thirdPersonItem(pose, kneelingModel(), arm, ticks)
        pose.mulPose(Axis.XP.rotationDegrees(-90F))
        pose.mulPose(Axis.YP.rotationDegrees(180F))
        pose.translate(sign / 16F, 2F / 16F, -10F / 16F)
        pose.translate(0F, 4F / 16F, 0.5F / 16F)
        pose.mulPose(Quaternionf().rotationXYZ(0F, -90F * Mth.DEG_TO_RAD, 55F * Mth.DEG_TO_RAD))
        return Matrix4f(pose.last().pose())
    }

    private fun ModelPart.at(pivot: Vector3f) {
        x = pivot.x
        y = pivot.y
        z = pivot.z
    }

    private fun ModelPart.pivot(): Vector3f = Vector3f(x, y, z)

    private fun ModelPart.axis(): Vector3f =
        Vector3f(sin(xRot) * sin(yRot), cos(xRot), sin(xRot) * cos(yRot))

    private fun ModelPart.slide(shoulder: Vector3f): Vector3f = pivot().sub(shoulder)

    private fun ModelPart.hand(): Vector3f = pivot().add(axis().mul(ARM_REACH))

    private fun firstPersonArm(arm: HumanoidArm, ticks: Float): Vector3f =
        PoseStack().also { animation.firstPersonArm(it, arm, ticks) }.last().pose()
            .getTranslation(Vector3f())

    @Test
    fun `third-person arms aim as mirror images`() {
        for (t in ticks) {
            val (right, left) = arms(t)
            assertEquals(right.xRot, left.xRot, 1e-6F)
            assertEquals(right.yRot, -left.yRot, 1e-6F)
        }
    }

    @Test
    fun `third-person arms slide along their own length`() {
        for (t in ticks) {
            val (right, left) = arms(t)
            assertEquals(0F, right.slide(rightShoulder).cross(right.axis()).length(), 1e-4F)
            assertEquals(0F, left.slide(leftShoulder).cross(left.axis()).length(), 1e-4F)
        }
    }

    @Test
    fun `third-person palms stroke in opposite directions`() {
        for (t in ticks) {
            val (right, left) = arms(t)
            val rightStroke = right.slide(rightShoulder).dot(right.axis())
            val leftStroke = left.slide(leftShoulder).dot(left.axis())
            assertEquals(rightStroke, -leftStroke, 1e-5F)
        }
    }

    @Test
    fun `third-person stroke repeats each period`() {
        for (t in ticks) {
            val (now, _) = arms(t)
            val (later, _) = arms(t + period)
            assertEquals(
                now.slide(rightShoulder).dot(now.axis()),
                later.slide(rightShoulder).dot(later.axis()),
                1e-3F,
            )
        }
    }

    @Test
    fun `third-person hands work down the spindle, then regrip at the top`() {
        val halfStrokes = (0 until 12).map { it * period / 2F }
        val heights = halfStrokes.map { arms(it).first.hand().y }
        for ((above, below) in heights.zipWithNext()) assertTrue(below > above, "$heights")
        assertEquals(heights.first(), arms(HandDrillAnimation.DESCENT_TICKS).first.hand().y, 1e-3F)
    }

    @Test
    fun `first-person arms rest as mirror images`() {
        val right = firstPersonArm(HumanoidArm.RIGHT, 0F)
        val left = firstPersonArm(HumanoidArm.LEFT, 0F)
        assertEquals(right.x, -left.x, 1e-5F)
        assertEquals(right.y, left.y, 1e-5F)
        assertEquals(right.z, left.z, 1e-5F)
    }

    @Test
    fun `first-person palms stroke in opposite directions`() {
        val rightRest = firstPersonArm(HumanoidArm.RIGHT, 0F)
        val leftRest = firstPersonArm(HumanoidArm.LEFT, 0F)
        for (t in ticks) {
            val right = firstPersonArm(HumanoidArm.RIGHT, t).sub(rightRest)
            val left = firstPersonArm(HumanoidArm.LEFT, t).sub(leftRest)
            assertEquals(right.z, -left.z, 1e-5F)
        }
    }

    @Test
    fun `first-person stroke repeats each period`() {
        for (t in ticks) {
            assertEquals(
                firstPersonArm(HumanoidArm.RIGHT, t).z,
                firstPersonArm(HumanoidArm.RIGHT, t + period).z,
                1e-4F,
            )
        }
    }

    // What vanilla applies after our first-person hook: the arm offset, then item/handheld's
    // first-person display (both hands end up with the same rotation), then the model centring.
    private fun firstPersonStick(arm: HumanoidArm, ticks: Float): Matrix4f {
        val sign = if (arm == HumanoidArm.RIGHT) 1F else -1F
        val pose = PoseStack().also { animation.firstPerson(it, arm, 0F, ticks) }
        pose.translate(sign * 0.56F, -0.52F, -0.72F)
        pose.translate(sign * 1.13F / 16F, 3.2F / 16F, 1.13F / 16F)
        pose.mulPose(Quaternionf().rotationXYZ(0F, -90F * Mth.DEG_TO_RAD, 25F * Mth.DEG_TO_RAD))
        return Matrix4f(pose.last().pose())
    }

    @Test
    fun `first-person stick stays in one place as it spins, in either hand`() {
        val centre = firstPersonStick(HumanoidArm.RIGHT, 0F).getTranslation(Vector3f())
        assertEquals(0F, centre.x, 1e-5F)
        for (t in ticks) {
            for (arm in HumanoidArm.entries) {
                val moved = firstPersonStick(arm, t).getTranslation(Vector3f())
                assertEquals(0F, moved.distance(centre), 1e-5F, "$arm at $t")
            }
        }
    }

    @Test
    fun `first-person stick stands upright, in either hand`() {
        for (t in ticks) {
            for (arm in HumanoidArm.entries) {
                val stick = firstPersonStick(arm, t).transformDirection(Vector3f(1F, 1F, 0F))
                assertEquals(0F, stick.normalize().distance(Vector3f(0F, 1F, 0F)), 1e-5F, "$arm at $t")
            }
        }
    }

    @Test
    fun `third-person stick stands in front of the body as it spins, in either hand`() {
        for (t in ticks) {
            for (arm in HumanoidArm.entries) {
                val centre = thirdPersonStick(arm, t).getTranslation(Vector3f()).mul(16F)
                assertEquals(0F, centre.distance(Vector3f(0F, 14F, -7F)), 1e-3F, "$arm at $t")
            }
        }
    }

    @Test
    fun `third-person stick stands upright, in either hand`() {
        for (t in ticks) {
            for (arm in HumanoidArm.entries) {
                val stick = thirdPersonStick(arm, t).transformDirection(Vector3f(1F, 1F, 0F))
                // model space has +Y down
                assertEquals(0F, stick.normalize().distance(Vector3f(0F, -1F, 0F)), 1e-5F, "$arm at $t")
            }
        }
    }

    @Test
    fun `third-person stick faces forward at rest`() {
        val face = thirdPersonStick(HumanoidArm.RIGHT, 0F).transformDirection(Vector3f(0F, 0F, 1F))
        assertEquals(0F, face.normalize().distance(Vector3f(0F, 0F, -1F)), 1e-5F)
    }

    private companion object {
        const val ARM_REACH = 10F
    }
}
