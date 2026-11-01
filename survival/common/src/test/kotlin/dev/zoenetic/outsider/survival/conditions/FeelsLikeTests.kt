package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.units.CALM
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Humidity
import dev.zoenetic.outsider.survival.units.Sky
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.units.Wind
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeelsLikeTests {

    private fun conditions(
        air: Double,
        humidity: Double,
        radiant: Double = 0.0,
        wind: Wind = CALM,
    ): PlayerConditions = PlayerConditions(
        humidity = Humidity(humidity),
        isUnderOpenSky = true,
        sky = Sky(1.0),
        ambient = Heat(air),
        ground = Heat(air),
        radiant = Heat(radiant),
        vapourPressure = vapourPressure(Humidity(humidity), Heat(air)),
        wind = wind,
        windExposure = 1.0,
        time = Time(0L),
    )

    private fun feelsLike(
        air: Double,
        humidity: Double,
        radiant: Double = 0.0,
        wind: Wind = CALM,
    ): Double = conditions(air, humidity, radiant, wind).feelsLike().celsius

    @Test
    fun `in calm air a fire adds exactly its radiant heat, however humid`() {
        for (humidity in listOf(0.0, 0.5, 1.0)) {
            val gained = feelsLike(30.0, humidity, radiant = 25.0) - feelsLike(30.0, humidity)
            assertEquals(25.0, gained, 1e-9, "at humidity $humidity")
        }
    }

    @Test
    fun `humid air feels warmer than dry air at the same temperature`() {
        assertTrue(feelsLike(30.0, 0.9) > feelsLike(30.0, 0.1))
    }

    @Test
    fun `wind as strong as the damping constant halves a fire's warmth`() {
        val wind = Wind(Vec3(RADIANT_WIND_DAMPING, 0.0, 0.0))
        val gained =
            feelsLike(10.0, 0.5, radiant = 8.0, wind = wind) - feelsLike(10.0, 0.5, wind = wind)
        assertEquals(4.0, gained, 1e-9)
    }
}
