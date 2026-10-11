package dev.zoenetic.outsider.survival.vitals.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.zoenetic.outsider.survival.vitals.client.ShiveringAnimation.Companion.SHIVER_AMPLITUDE_DEGREES
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import org.joml.Matrix4f
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShiveringAnimationTests {

    private val animation = ShiveringAnimation()
    private val ticks = (0 until 200).map { it * 0.37F }

    private fun bodyRotation(strength: Float, ticks: Float): Float =
        animation.bodyRotation(strength, LivingEntityRenderState().apply { ageInTicks = ticks })

    private fun firstPerson(strength: Float, ticks: Float): Matrix4f =
        PoseStack().also { animation.firstPerson(strength, it, ticks) }.last().pose()

    @Test
    fun `no strength, no body wobble`() {
        assertTrue(ticks.all { bodyRotation(0F, it) == 0F })
    }

    @Test
    fun `body wobble scales with strength`() {
        for (t in ticks) assertEquals(bodyRotation(1F, t) / 2, bodyRotation(0.5F, t), 1e-5F)
    }

    @Test
    fun `body wobble stays within the amplitude`() {
        val amplitude = SHIVER_AMPLITUDE_DEGREES.toFloat()
        assertTrue(ticks.all { kotlin.math.abs(bodyRotation(1F, it)) <= amplitude + 1e-5F })
        assertTrue(ticks.any { kotlin.math.abs(bodyRotation(1F, it)) > amplitude * 0.9F })
    }

    @Test
    fun `no strength leaves the held items where they are`() {
        assertTrue(ticks.all { firstPerson(0F, it).equals(Matrix4f(), 1e-6F) })
    }

    @Test
    fun `strength moves the held items`() {
        assertTrue(ticks.any { !firstPerson(1F, it).equals(Matrix4f(), 1e-6F) })
    }
}
