package dev.zoenetic.outsider.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.units.Conductance
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Insulation
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import kotlin.math.pow

internal val NORMAL_BODY_TEMPERATURE: Heat = Heat(37.0)
internal const val BODY_TEMP_INCREASE_PER_CAPACITY: Double = 2.0

public const val BODY_COOLS_AT: Double = 30.0 * 60.0
public const val BODY_WARMS_AT: Double = 15.0 * 60.0

public val COMFORT_LOW: Heat = Heat(20.0)
public val COMFORT_HIGH: Heat = Heat(30.0)
public val COLD_LEAKAGE: Heat = Heat(0.5)
public val HEAT_LEAKAGE: Heat = Heat(0.2)


public data class BodyTemperature(
    val heat: Heat = Heat(37.0)
) {
    public fun getNew(
        player: Player,
        conditions: PlayerConditions,
        exertion: MET,
        elapsed: Duration
    ): BodyTemperature {
        val current = heat
        val insulation = player.getInsulation()
        val fromExertion = BODY_TEMP_INCREASE_PER_CAPACITY * exertion.capacity(MET_MAX)
        val target = target(conditions.feelsLike(), insulation) + Heat(fromExertion)
        if (current == target) return this
        val isWarming = current < target
        val medium = player.getConductanceOfMediumIn()
        val surface = player.getConductanceOfSurfaceOn()
        val wind = conditions.wind.conductance
        val halfLifeSeconds = halfLife(isWarming, medium, surface, wind, insulation)
        val new = approach(
            current,
            target,
            elapsed,
            halfLifeSeconds,
        )
        return BodyTemperature(new)
    }


    public companion object {

        internal fun target(
            ambient: Heat,
            insulation: Insulation = Insulation.NONE,
        ): Heat {
            val ambient = ambient
            val target = when {
                ambient < COMFORT_LOW ->
                    NORMAL_BODY_TEMPERATURE - (COLD_LEAKAGE / insulation.value) * (COMFORT_LOW - ambient)

                ambient > COMFORT_HIGH ->
                    NORMAL_BODY_TEMPERATURE + (HEAT_LEAKAGE * insulation.value) * (ambient - COMFORT_HIGH)

                else -> NORMAL_BODY_TEMPERATURE
            }
            return target
        }

        internal fun halfLife(
            isWarming: Boolean,
            medium: Conductance? = null,
            surface: Conductance? = null,
            wind: Conductance? = null,
            insulation: Insulation = Insulation.NONE,
        ): Double {
            val medium = medium?.value
            val surface = surface?.value
            val wind = wind?.value
            val conductance =
                (medium ?: 1.0) * (surface ?: 1.0) * (wind ?: 1.0) / insulation.value
            return if (isWarming) BODY_WARMS_AT / conductance else BODY_COOLS_AT / conductance
        }

        internal fun approach(
            current: Heat,
            target: Heat,
            elapsed: Duration,
            halfLife: Double,
        ): Heat {
            val elapsedSeconds = elapsed.value / SharedConstants.TICKS_PER_SECOND.toDouble()
            val remainingFraction = 0.5.pow(elapsedSeconds / halfLife)
            return target + (current - target) * remainingFraction
        }

        public val CODEC: Codec<BodyTemperature> =
            Heat.CODEC.xmap(
                ::BodyTemperature,
                BodyTemperature::heat
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> =
            Heat.STREAM_CODEC.map(
                ::BodyTemperature,
                BodyTemperature::heat
            )

        public val DEFAULT: BodyTemperature = BodyTemperature(NORMAL_BODY_TEMPERATURE)
    }
}
