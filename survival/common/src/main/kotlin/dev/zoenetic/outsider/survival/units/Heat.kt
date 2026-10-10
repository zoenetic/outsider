package dev.zoenetic.outsider.survival.units

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

@JvmInline
public value class Heat(public val celsius: Double) : Comparable<Heat> {
    public operator fun plus(o: Heat): Heat = Heat(celsius + o.celsius)
    public operator fun minus(o: Heat): Heat = Heat(celsius - o.celsius)
    public operator fun div(o: Heat): Heat = Heat(celsius / o.celsius)
    public operator fun div(d: Double): Heat = Heat(celsius / d)
    public operator fun times(o: Heat): Heat = Heat(celsius * o.celsius)
    public operator fun times(d: Double): Heat = Heat(celsius * d)
    override fun compareTo(other: Heat): Int = compareValues(celsius, other.celsius)
    override fun toString(): String = "$celsius°C"

    public fun coerceAtLeast(minimum: Double): Heat =
        if (this.celsius < minimum) Heat(minimum) else this

    public fun coerceAtMost(maximum: Heat): Heat = if (this.celsius >
        maximum.celsius
    ) {
        Heat(maximum.celsius)
    } else {
        this
    }

    public companion object {
        public val ZERO: Heat = Heat(0.0)

        public val CODEC: Codec<Heat> =
            Codec.DOUBLE.xmap(
                ::Heat,
                Heat::celsius,
            )
        public val STREAM_CODEC: StreamCodec<ByteBuf, Heat> =
            ByteBufCodecs.DOUBLE.map(
                ::Heat,
                Heat::celsius,
            )
    }
}
