package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.emission.EmitterIndex.EMISSION_SOFTENING
import dev.zoenetic.outsider.survival.emission.EmitterIndex.heatFrom
import dev.zoenetic.outsider.survival.units.Heat
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeatFalloffTests {
    private val body = Vec3(0.5, 0.5, 0.5)

    @Test
    fun `a more distant source contributes less than a nearer one`() {
        val near = heatFrom(body, BlockPos(2, 0, 0), Heat(25.0))
        val far = heatFrom(body, BlockPos(6, 0, 0), Heat(25.0))
        assertTrue(far < near, "expected $far to be less than $near")
    }

    @Test
    fun `two identical heat sources at equal distance give exactly double`() {
        val power = Heat(10.0)
        val one = heatFrom(body, BlockPos(3, 0, 0), power)
        val two = one + heatFrom(body, BlockPos(0, 0, 3), power)
        assertEquals(2 * one.celsius, two.celsius, 1e-9, "expected $two to be twice $one")
    }

    @Test
    fun `a source at zero distance contributes exactly its power over the softening`() {
        val power = Heat(10.0)
        assertEquals(
            power.celsius / EMISSION_SOFTENING,
            heatFrom(body, BlockPos(0, 0, 0), power).celsius,
            1e-9
        )
    }

    @Test
    fun `contribution keeps rising all the way in, with no plateau`() {
        val source = BlockPos(0, 0, 0)
        val distances = listOf(2.0, 1.0, 0.5, 0.25, 0.1, 0.0)
        val heats = distances.map { d -> heatFrom(Vec3(0.5, 0.5, 0.5 + d), source, Heat(10.0)) }
        for (i in 1 until heats.size) {
            assertTrue(
                heats[i] > heats[i - 1],
                "at ${distances[i]} (${heats[i]}) should exceed at ${distances[i - 1]} (${heats[i - 1]})"
            )
        }
    }

    @Test
    fun `far from the source the falloff is inverse square`() {
        val power = Heat(10.0)
        val distance = 20.0
        val softened = heatFrom(Vec3(0.5, 0.5, 0.5 + distance), BlockPos(0, 0, 0), power)
        val inverseSquare = power.celsius / (distance * distance)
        val relativeError = abs(softened.celsius - inverseSquare) / inverseSquare
        assertTrue(
            relativeError < 0.005,
            "at $distance blocks softening should be negligible; relative error $relativeError"
        )
    }
}
