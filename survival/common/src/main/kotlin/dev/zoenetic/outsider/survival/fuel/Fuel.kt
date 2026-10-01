package dev.zoenetic.outsider.survival.fuel

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

public data class Fuel(public val level: Int) {
    init {
        require(level in 0..15) { "fuel level out of range: $level" }
    }

    public operator fun plus(other: Fuel): Fuel =
        Fuel((level + other.level).coerceAtMost(MAX.level))

    public operator fun compareTo(other: Fuel): Int = level.compareTo(other.level)

    public fun coerceAtMost(maximum: Fuel): Fuel {
        return if (this.level > maximum.level) maximum else this
    }

    public companion object {
        public val EMPTY: Fuel = Fuel(0)
        public val MAX: Fuel = Fuel(15)

        public val CODEC: Codec<Fuel> = Codec.intRange(0, MAX.level).xmap(
            ::Fuel,
            Fuel::level,
        )
        public val STREAM_CODEC: StreamCodec<ByteBuf, Fuel> = ByteBufCodecs.VAR_INT.map(
            ::Fuel,
            Fuel::level
        )
    }
}
