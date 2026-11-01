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
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import kotlin.math.min

internal val NORMAL_BODY_TEMPERATURE: Heat = Heat(37.0)

internal const val BASAL_HEAT: Double = 8.75
internal const val EXERTION_HEAT_FRACTION: Double = 0.2
internal const val MAX_SWEAT_COOLING: Double = 20.0
internal const val BODY_HEAT_CAPACITY: Double = 600.0

internal const val TISSUE_RESISTANCE_CONSTRICTED: Double = 1.0
internal const val TISSUE_RESISTANCE_DILATED: Double = 0.05
internal val VASOCONSTRICTED_BELOW: Heat = Heat(36.8)
internal val VASODILATED_ABOVE: Heat = Heat(37.2)

internal val SWEAT_ONSET: Heat = Heat(37.2)
internal val SWEAT_FULL: Heat = Heat(38.0)

internal val INTEGRATION_STEP: Duration = Duration(20L)
internal val MAX_CATCH_UP: Duration = Duration(20L * 60L * 10L)

public data class BodyTemperature(val heat: Heat = NORMAL_BODY_TEMPERATURE) {
    public fun getNew(
        player: Player,
        conditions: PlayerConditions,
        exertion: MET,
        elapsed: Duration,
    ): BodyTemperature {
        val contacts = player.getContacts(conditions)
        val new = step(
            heat,
            contacts.environment(),
            contacts.conductance(),
            player.getInsulation(),
            exertion,
            elapsed,
        )
        return BodyTemperature(new)
    }

    public companion object {

        internal fun step(
            body: Heat,
            environment: Heat,
            conductance: Conductance,
            insulation: Insulation,
            exertion: MET,
            elapsed: Duration,
        ): Heat {
            var t = body
            var remaining = min(elapsed.value, MAX_CATCH_UP.value)
            while (remaining > 0) {
                val ticks = min(remaining, INTEGRATION_STEP.value)
                val seconds = ticks / SharedConstants.TICKS_PER_SECOND.toDouble()
                t += Heat(rate(t, environment, conductance, insulation, exertion) * seconds)
                remaining -= ticks
            }
            return t
        }

        public fun rate(
            body: Heat,
            environment: Heat,
            conductance: Conductance,
            insulation: Insulation,
            exertion: MET,
        ): Double {
            val net = production(body, exertion) - loss(body, environment, conductance, insulation)
            return net / BODY_HEAT_CAPACITY
        }

        internal fun production(body: Heat, exertion: MET): Double =
            BASAL_HEAT * (1.0 + EXERTION_HEAT_FRACTION * (exertion.value - 1.0)) -
                MAX_SWEAT_COOLING * sweating(body)

        internal fun loss(
            body: Heat,
            environment: Heat,
            conductance: Conductance,
            insulation: Insulation,
        ): Double {
            val resistance = tissueResistance(body) + insulation.value / conductance.value
            return (body - environment).celsius / resistance
        }

        internal fun tissueResistance(body: Heat): Double = Mth.clampedMap(
            body.celsius,
            VASOCONSTRICTED_BELOW.celsius,
            VASODILATED_ABOVE.celsius,
            TISSUE_RESISTANCE_CONSTRICTED,
            TISSUE_RESISTANCE_DILATED,
        )

        internal fun sweating(body: Heat): Double =
            Mth.clampedMap(body.celsius, SWEAT_ONSET.celsius, SWEAT_FULL.celsius, 0.0, 1.0)

        public val CODEC: Codec<BodyTemperature> =
            Heat.CODEC.xmap(
                ::BodyTemperature,
                BodyTemperature::heat,
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> =
            Heat.STREAM_CODEC.map(
                ::BodyTemperature,
                BodyTemperature::heat,
            )

        public val DEFAULT: BodyTemperature = BodyTemperature(NORMAL_BODY_TEMPERATURE)
    }
}
