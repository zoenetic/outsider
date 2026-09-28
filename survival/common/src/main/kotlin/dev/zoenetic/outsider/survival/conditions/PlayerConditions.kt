package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.climate.getClimate
import dev.zoenetic.outsider.survival.climate.getTemperature
import dev.zoenetic.outsider.survival.climate.getWind
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.units.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import kotlin.math.exp

private val INTERVAL = Duration(20L)

internal const val HEAT_TRAPPING: Double = 2.0
internal val MAX_HEATED_AIR: Heat = Heat(30.0)

public class PlayerConditions(
    public val humidity: Humidity,
    public val isUnderOpenSky: Boolean,
    public val sky: Sky,
    public val ambient: Heat,
    public val radiant: Heat,
    public val wind: Wind,
    public val windExposure: Double,
    public val time: Time,
) {
    public fun feelsLike(): Heat {
        val h = humidity.value
        val t = ambient.celsius
        val e = h * 6.105 * exp(17.27 * t / (237.7 + t))
        val w = wind.speed
        return Heat(t + 0.33 * e - 0.70 * w - 4.00)
    }

    public companion object {

        public fun get(player: ServerPlayer): PlayerConditions? =
            Survival.platform.playerConditions.get(player)

        public fun getNew(player: ServerPlayer, time: Time): PlayerConditions {
            val level = player.level()
            val pos = player.blockPosition()
            val chunk = level.getChunkAt(pos)
            val chunkClimate = chunk.getClimate()
            val sky = player.getSky()
            val ambient = chunk.getTemperature(
                Altitude(pos.y - level.seaLevel),
                chunkClimate.humidity, sky, Time(level.overworldClockTime)
            )
            val radiant = EmitterIndex.heatAtPlayer(player)
            val heat = trapHeat(radiant, sky, ambient)
            val chunkWind = chunk.getWind()
            val windExposure = player.getWindExposure(chunkWind)
            val wind = chunkWind.sheltered(windExposure)
            return PlayerConditions(
                chunkClimate.humidity,
                player.isUnderOpenSky(),
                sky,
                ambient + heat,
                heat,
                wind,
                windExposure,
                time
            )
        }

        public fun set(player: ServerPlayer, conditions: PlayerConditions) {
            Survival.platform.playerConditions.set(player, conditions)
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
            radiant = Heat(0.0),
            wind = CALM,
            windExposure = 1.0,
            time = Time(0L),
        )
    }
}

internal fun trapHeat(
    radiant: Heat,
    sky: Sky,
    ambient: Heat
): Heat {
    val enclosure = (1.0 - sky.value).coerceIn(0.0, 1.0)
    val headroom = (MAX_HEATED_AIR - ambient - radiant).coerceAtLeast(0.0)
    val trapped = (radiant * HEAT_TRAPPING * enclosure).coerceAtMost(headroom)
    return radiant + trapped
}
