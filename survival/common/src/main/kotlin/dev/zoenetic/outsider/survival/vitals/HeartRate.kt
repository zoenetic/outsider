package dev.zoenetic.outsider.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.StreamCodec
import kotlin.math.pow

internal val RESTING_HEART_RATE = BPM(75.0)
internal val MAX_HEART_RATE = BPM(200.0)

internal val ASYSTOLE_TEMPERATURE = Heat(22.0)
internal val ARRHYTHMIA_TEMPERATURE = Heat(44.0)

internal const val LOW_BPM_AUDIBLE_THRESHOLD = 60.0
internal const val LOW_BPM_FULL_VOLUME_THRESHOLD = 40.0

internal const val HIGH_BPM_AUDIBLE_THRESHOLD = 90.0
internal const val HIGH_BPM_FULL_VOLUME_THRESHOLD = 160.0

internal const val HEART_RATE_RISES_AT = 20.0
internal const val HEART_RATE_FALLS_AT = 60.0

public data class Heartbeat(
    val interval: Duration,
    val pitch: Float,
    val volume: Float,
)

public data class HeartRate(
    val bpm: BPM = BPM(75.0)
) {
    public fun getNew(
        bodyTemperature: Heat,
        exertion: MET,
        elapsed: Duration
    ): HeartRate {
        val current = bpm.value
        val chill =
            ((bodyTemperature - ASYSTOLE_TEMPERATURE) / (SHIVER_CEASES - ASYSTOLE_TEMPERATURE)).coerceIn(
                Heat(0.0),
                Heat(1.0)
            )
        val heatDelta =
            10 * (bodyTemperature - NORMAL_BODY_TEMPERATURE).coerceIn(
                Heat(0.0),
                ARRHYTHMIA_TEMPERATURE - NORMAL_BODY_TEMPERATURE
            ).celsius.pow(1.3)
        val reserve = MAX_HEART_RATE - RESTING_HEART_RATE
        val exerted = RESTING_HEART_RATE + reserve * exertion.capacity(MET_MAX)
        val target =
            ((exerted + heatDelta) * chill.celsius).value.coerceAtMost(MAX_HEART_RATE.value)
        val isRising = target > current
        val halfLife = halfLife(isRising)
        val new = approach(
            current,
            target,
            elapsed,
            halfLife,
        )
        return HeartRate(
            BPM(new)
        )
    }

    public fun toHeartbeat(): Heartbeat? {
        if (bpm.value <= 0.0) return null
        val interval = (SharedConstants.TICKS_PER_MINUTE / bpm.value).toLong()
        val pitch = 1F
        val volume = volume()
        return Heartbeat(
            Duration(interval), pitch, volume
        )
    }

    public fun volume(): Float {
        val actual = bpm.value
        val ramp = when {
            actual < LOW_BPM_AUDIBLE_THRESHOLD ->
                (LOW_BPM_AUDIBLE_THRESHOLD - actual) /
                        (LOW_BPM_AUDIBLE_THRESHOLD - LOW_BPM_FULL_VOLUME_THRESHOLD)

            actual > HIGH_BPM_AUDIBLE_THRESHOLD ->
                (actual - HIGH_BPM_AUDIBLE_THRESHOLD) /
                        (HIGH_BPM_FULL_VOLUME_THRESHOLD - HIGH_BPM_AUDIBLE_THRESHOLD)

            else -> 0.0
        }
        return ramp.coerceIn(0.0, 1.0).toFloat()
    }

    public companion object {

        internal fun halfLife(
            isRising: Boolean
        ): Double {
            return if (isRising) HEART_RATE_RISES_AT else HEART_RATE_FALLS_AT
        }

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

        public val CODEC: Codec<HeartRate> =
            BPM.CODEC.xmap(
                ::HeartRate,
                HeartRate::bpm
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            BPM.STREAM_CODEC.map(
                ::HeartRate,
                HeartRate::bpm
            )

        public val DEFAULT: HeartRate = HeartRate(RESTING_HEART_RATE)
    }
}