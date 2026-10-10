package dev.zoenetic.outsider.survival.units

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

@JvmInline
public value class Time(public val value: Long) {
    public operator fun plus(o: Duration): Time = Time(value + o.value)
    public operator fun minus(o: Time): Duration = Duration(value - o.value)
    public operator fun minus(o: Duration): Time = Time(value - o.value)
    public operator fun times(k: Int): Duration = Duration(value * k)
    public operator fun compareTo(o: Time): Int = value.compareTo(o.value)

    public companion object {
        public val CODEC: Codec<Time> =
            Codec.LONG.xmap(
                ::Time,
                Time::value,
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, Time> =
            ByteBufCodecs.LONG.map(
                ::Time,
                Time::value,
            )
    }
}
