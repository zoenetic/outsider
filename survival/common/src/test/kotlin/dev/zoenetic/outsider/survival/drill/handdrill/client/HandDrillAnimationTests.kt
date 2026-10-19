package dev.zoenetic.outsider.survival.drill.handdrill.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.zoenetic.outsider.survival.tools.drill.handdrill.client.HandDrillAnimation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.entity.HumanoidArm
import org.joml.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals

class HandDrillAnimationTests {

    private val animation = HandDrillAnimation()
    private val ticks = (0 until 60).map { it * 0.41F }

    // Three strokes a second, so the pose repeats every 20 / 3 ticks.
    private val period = 20F / 3F

    private fun arms(ticks: Float): Pair<ModelPart, ModelPart> {
        val right = ModelPart(emptyList(), emptyMap())
        val left = ModelPart(emptyList(), emptyMap())
        animation.thirdPerson(right, left, ticks)
        return right to left
    }

    private fun firstPerson(arm: HumanoidArm, ticks: Float): Vector3f =
        PoseStack().also { animation.firstPerson(it, arm, 0F, ticks) }.last().pose()
            .getTranslation(Vector3f())

    @Test
    fun `third-person arms mirror each other`() {
        for (t in ticks) {
            val (right, left) = arms(t)
            assertEquals(right.xRot, left.xRot)
            assertEquals(right.yRot, -left.yRot)
            assertEquals(right.z, -left.z, 1e-6F)
        }
    }

    @Test
    fun `third-person stroke repeats each period`() {
        for (t in ticks) assertEquals(arms(t).first.z, arms(t + period).first.z, 1e-3F)
    }

    @Test
    fun `first-person arms mirror each other`() {
        for (t in ticks) {
            val right = firstPerson(HumanoidArm.RIGHT, t)
            val left = firstPerson(HumanoidArm.LEFT, t)
            assertEquals(right.x, -left.x, 1e-5F)
            assertEquals(right.y, left.y, 1e-5F)
        }
    }

    @Test
    fun `first-person stroke repeats each period`() {
        for (t in ticks) {
            assertEquals(
                firstPerson(HumanoidArm.RIGHT, t).z,
                firstPerson(
                    HumanoidArm.RIGHT,
                    t + period,
                ).z,
                1e-4F,
            )
        }
    }
}
