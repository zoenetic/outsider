package dev.zoenetic.outsider.survival.units

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.math.roundToLong

@JvmInline
public value class Duration(public val value: Long) {
    public operator fun plus(o: Duration): Duration = Duration(this.value + o.value)
    public operator fun minus(o: Duration): Duration = Duration(this.value - o.value)
    public operator fun times(k: Int): Duration = Duration(this.value * k)
    public operator fun times(k: Double): Duration = Duration((this.value * k).roundToLong())
    public operator fun compareTo(o: Duration): Int = value.compareTo(o.value)

    public companion object {
        public val NONE: Duration = Duration(0)

        public val STREAM_CODEC: StreamCodec<ByteBuf, Duration> = ByteBufCodecs.LONG.map(
            ::Duration,
            Duration::value,
        )
    }
}
