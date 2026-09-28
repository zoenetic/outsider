package dev.zoenetic.outsider.survival.climate

import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.units.Wind
import net.minecraft.world.level.ChunkPos
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RealNoiseSmokeTests {

    private val level = CommonFixtures.climateLevel()

    private fun at(x: Int, z: Int) = CommonFixtures.climateChunk(ChunkPos(x, z), level)

    private val sample = (0..19).map { at(it * 7, it * -5) }

    @Test
    fun `humidity is not the same everywhere`() {
        val humidities = sample.map { it.getHumidity().value }
        assertTrue(
            humidities.distinct().size >= humidities.size - 1,
            "real noise should give a distinct humidity almost everywhere, got $humidities"
        )
    }

    @Test
    fun `temperature is not the same everywhere`() {
        val temperatures = sample.map { it.getBaseTemperature().celsius }
        assertTrue(
            temperatures.distinct().size >= temperatures.size - 1,
            "real noise should give a distinct temperature almost everywhere, got $temperatures"
        )
    }

    @Test
    fun `humidity and temperature come from different noise fields`() {
        val byHumidity = sample.sortedBy { it.getHumidity().value }.map { it.pos }
        val byTemperature = sample.sortedBy { it.getBaseTemperature().celsius }.map { it.pos }
        assertNotEquals(
            byHumidity,
            byTemperature,
            "humidity and temperature rank identically, so they are reading one field"
        )
    }

    @Test
    fun `the same chunk position always samples the same climate`() {
        assertEquals(
            at(7, -13).getClimate(),
            at(7, -13).getClimate(),
            "the sampler must be deterministic for a given seed and position"
        )
    }

    @Test
    fun `wind is presently derived from the temperature field`() {
        val chunk = at(3, 11)
        val fromTemperature = Wind.fromDensityFunction(
            CommonFixtures.randomState.sampler().temperature(),
            chunk,
        )
        assertEquals(
            fromTemperature.vector,
            chunk.getWind().vector,
            "wind no longer tracks the temperature field — if that was deliberate, update this"
        )
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
