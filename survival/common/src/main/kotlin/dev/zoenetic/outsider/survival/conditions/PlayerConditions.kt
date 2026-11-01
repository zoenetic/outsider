package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.climate.getClimate
import dev.zoenetic.outsider.survival.climate.getGroundTemperature
import dev.zoenetic.outsider.survival.climate.getTemperature
import dev.zoenetic.outsider.survival.climate.getWind
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.units.Altitude
import dev.zoenetic.outsider.survival.units.CALM
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Humidity
import dev.zoenetic.outsider.survival.units.Sky
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.units.Wind
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import kotlin.math.exp

private val INTERVAL = Duration(20L)

internal const val HEAT_TRAPPING: Double = 2.0
internal val MAX_HEATED_AIR: Heat = Heat(30.0)
internal const val RADIANT_WIND_DAMPING: Double = 10.0

public class PlayerConditions(
    public val humidity: Humidity,
    public val isUnderOpenSky: Boolean,
    public val sky: Sky,
    public val ambient: Heat,
    public val ground: Heat,
    public val radiant: Heat,
    public val vapourPressure: Double,
    public val wind: Wind,
    public val windExposure: Double,
    public val time: Time,
) {
    public fun feelsLike(): Heat {
        val t = ambient.celsius
        val e = vapourPressure
        val r = radiant.celsius * RADIANT_WIND_DAMPING / (wind.speed + RADIANT_WIND_DAMPING)
        return Heat(t + 0.33 * e - 4.00 + r)
    }

    public companion object {

        public fun get(player: ServerPlayer): PlayerConditions? =
            Survival.platform.conditions.get(player)

        public fun getNew(player: ServerPlayer, time: Time): PlayerConditions {
            val level = player.level()
            val pos = player.blockPosition()
            val chunk = level.getChunkAt(pos)
            val chunkClimate = chunk.getClimate()
            val sky = player.getSky()
            val altitude = Altitude(pos.y - level.seaLevel)
            val air = chunk.getTemperature(
                altitude,
                chunkClimate.humidity,
                sky,
                Time(level.overworldClockTime),
            )
            val radiant = EmitterIndex.heatAtPlayer(player)
            val trapped = trapHeat(radiant, sky, air)
            val chunkWind = chunk.getWind()
            val windExposure = player.getWindExposure(chunkWind)
            val wind = chunkWind.sheltered(windExposure)
            return PlayerConditions(
                chunkClimate.humidity,
                player.isUnderOpenSky(),
                sky,
                air + trapped,
                chunk.getGroundTemperature(altitude),
                radiant,
                vapourPressure(chunkClimate.humidity, air),
                wind,
                windExposure,
                time,
            )
        }

        public fun set(player: ServerPlayer, conditions: PlayerConditions) {
            Survival.platform.conditions.set(player, conditions)
        }

        public fun tick(level: Level) {
            if (level !is ServerLevel) return
            val time = Time(level.gameTime)
            level.players().forEach { player -> tick(player, time) }
        }

        public fun tick(player: ServerPlayer, time: Time) {
            val previous = get(player)
            if (previous != null) {
                val elapsed = time - previous.time
                if (elapsed < INTERVAL) return
            }
            val conditions = getNew(player, time)
            set(player, conditions)
        }

        public val EMPTY: PlayerConditions = PlayerConditions(
            humidity = Humidity(0.5),
            isUnderOpenSky = false,
            sky = Sky(1.0),
            ambient = Heat(20.0),
            ground = Heat(20.0),
            radiant = Heat(0.0),
            vapourPressure = vapourPressure(Humidity(0.5), Heat(20.0)),
            wind = CALM,
            windExposure = 1.0,
            time = Time(0L),
        )
    }
}

internal fun trapHeat(radiant: Heat, sky: Sky, air: Heat): Heat {
    val enclosure = (1.0 - sky.value).coerceIn(0.0, 1.0)
    val headroom = (MAX_HEATED_AIR - air).coerceAtLeast(0.0)
    return (radiant * HEAT_TRAPPING * enclosure).coerceAtMost(headroom)
}

internal fun vapourPressure(humidity: Humidity, air: Heat): Double {
    val t = air.celsius
    return humidity.value * 6.105 * exp(17.27 * t / (237.7 + t))
}
