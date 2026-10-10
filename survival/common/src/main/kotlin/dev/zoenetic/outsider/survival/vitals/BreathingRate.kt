package dev.zoenetic.outsider.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.math.pow

internal const val RESTING_BREATHING_RATE = 14.0
internal const val MAX_BREATHING_RATE = 50.0

internal const val BREATHING_RATE_CURVE = 1.5

internal const val BREATHS_PER_DEGREE_OF_FEVER = 3.0
internal val APNOEA_TEMPERATURE = Heat(24.0)

internal const val BREATHING_RATE_RISES_AT = 30.0
internal const val BREATHING_RATE_FALLS_AT = 45.0

internal const val BREATHING_AUDIBLE_THRESHOLD = 20.0
internal const val BREATHING_FULL_VOLUME_THRESHOLD = 40.0

internal const val BREATHING_VISIBLE_THRESHOLD = 8.0
internal const val BREATHING_FULL_VISIBILITY_THRESHOLD = -5.0

public data class Breath(val interval: Duration, val pitch: Float, val volume: Float)

public data class BreathingRate(val value: Double = RESTING_BREATHING_RATE) {
    public fun getNew(bodyTemperature: Heat, exertion: MET, elapsed: Duration): BreathingRate {
        val current = value
        val chill =
            ((bodyTemperature - APNOEA_TEMPERATURE) / (SHIVER_CEASES - APNOEA_TEMPERATURE)).coerceIn(
                Heat(0.0),
                Heat(1.0),
            )
        val fever =
            (bodyTemperature - NORMAL_BODY_TEMPERATURE).coerceAtLeast(
                0.0,
            ) * BREATHS_PER_DEGREE_OF_FEVER
        val reserve = MAX_BREATHING_RATE - RESTING_BREATHING_RATE
        val exerted =
            RESTING_BREATHING_RATE + reserve * exertion.capacity(MET_MAX).coerceIn(0.0, 1.0)
                .pow(BREATHING_RATE_CURVE)
        val target = (chill.celsius * (exerted + fever.celsius)).coerceAtMost(MAX_BREATHING_RATE)
        val isRising = target > current
        val halfLife = halfLife(isRising)
        val new = approach(
            current,
            target,
            elapsed,
            halfLife,
        )
        return BreathingRate(new)
    }

    public fun toBreath(): Breath? {
        if (value <= 0.0) return null
        val interval = (SharedConstants.TICKS_PER_MINUTE / value).toLong()
        val pitch = 1F
        val volume = volume()
        return Breath(
            Duration(interval),
            pitch,
            volume,
        )
    }

    public fun visibility(ambient: Heat): Float {
        val airTemp = ambient.celsius
        if (airTemp >= BREATHING_VISIBLE_THRESHOLD) return 0F
        val range = BREATHING_VISIBLE_THRESHOLD - BREATHING_FULL_VISIBILITY_THRESHOLD
        return ((BREATHING_VISIBLE_THRESHOLD - airTemp) / range).coerceIn(0.0, 1.0).toFloat()
    }

    public fun volume(): Float {
        val actual = value
        if (actual >= BREATHING_AUDIBLE_THRESHOLD) {
            return (
                (actual - BREATHING_AUDIBLE_THRESHOLD) /
                    (BREATHING_FULL_VOLUME_THRESHOLD - BREATHING_AUDIBLE_THRESHOLD)
                ).coerceIn(
                0.0,
                1.0,
            ).toFloat()
        }
        return 0F
    }

    public companion object {

        internal fun halfLife(isRising: Boolean): Double =
            if (isRising) BREATHING_RATE_RISES_AT else BREATHING_RATE_FALLS_AT

        internal fun approach(
            current: Double,
            target: Double,
            elapsed: Duration,
            halfLife: Double,
        ): Double {
            val elapsedSeconds = elapsed.value / SharedConstants.TICKS_PER_SECOND.toDouble()
            val remainingFraction = 0.5.pow(elapsedSeconds / halfLife)
            return target + (current - target) * remainingFraction
        }

        public val CODEC: Codec<BreathingRate> =
            Codec.DOUBLE.xmap(
                ::BreathingRate,
                BreathingRate::value,
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, BreathingRate> =
            ByteBufCodecs.DOUBLE.map(
                ::BreathingRate,
                BreathingRate::value,
            )

        public val DEFAULT: BreathingRate = BreathingRate(RESTING_BREATHING_RATE)
    }
}
