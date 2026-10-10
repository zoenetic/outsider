package dev.zoenetic.outsider.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.Survival.MOD_ID
import dev.zoenetic.outsider.survival.units.Heat
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import kotlin.math.abs

internal val SPEED_PENALTY_MAX = SpeedPenalty(0.6)

internal const val SPEED_PENALTY_DEAD_ZONE = 1.0
internal val COLD_FULL_PENALTY_AT = Heat(28.0)
internal val HEAT_FULL_PENALTY_AT = Heat(41.0)

private val BODY_TEMPERATURE_SPEED_REDUCTION =
    Identifier.fromNamespaceAndPath(
        MOD_ID,
        "body_temperature_speed_reduction",
    )

@JvmInline
public value class SpeedPenalty(public val value: Double) {

    public companion object {
        public fun forTemperature(temperature: Heat): SpeedPenalty {
            val deviation =
                abs(temperature.celsius - NORMAL_BODY_TEMPERATURE.celsius)
            if (deviation <= SPEED_PENALTY_DEAD_ZONE) return SpeedPenalty(0.0)
            val fullAt =
                if (temperature < NORMAL_BODY_TEMPERATURE) COLD_FULL_PENALTY_AT else HEAT_FULL_PENALTY_AT
            val range =
                abs(fullAt.celsius - NORMAL_BODY_TEMPERATURE.celsius) - SPEED_PENALTY_DEAD_ZONE
            val progress =
                ((deviation - SPEED_PENALTY_DEAD_ZONE) / range).coerceIn(
                    0.0,
                    1.0,
                )
            return SpeedPenalty(SPEED_PENALTY_MAX.value * progress)
        }

        public fun tick(level: ServerLevel) {
            val players = level.players()
            players.forEach(this::tick)
        }

        public fun tick(player: ServerPlayer) {
            val vitals = Survival.platform.vitals.get(player) ?: return
            val penalty = forTemperature(vitals.bodyTemperature.heat)
            val attribute = player.getAttribute(MOVEMENT_SPEED) ?: return
            if (penalty.value == 0.0) {
                attribute.removeModifier(BODY_TEMPERATURE_SPEED_REDUCTION)
            } else {
                attribute.addOrUpdateTransientModifier(
                    AttributeModifier(
                        BODY_TEMPERATURE_SPEED_REDUCTION,
                        -penalty.value,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                    ),
                )
            }
        }

        public val CODEC: Codec<SpeedPenalty> =
            Codec.DOUBLE.xmap(
                ::SpeedPenalty,
                SpeedPenalty::value,
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, SpeedPenalty> =
            ByteBufCodecs.DOUBLE.map(
                ::SpeedPenalty,
                SpeedPenalty::value,
            )

        public val DEFAULT: SpeedPenalty = SpeedPenalty(0.0)
    }
}
