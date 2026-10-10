package dev.zoenetic.outsider.survival.vitals

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

@JvmInline
public value class BPM(public val value: Double) {

    public operator fun plus(o: BPM): BPM = BPM(value + o.value)
    public operator fun plus(k: Double): BPM = BPM(value + k)
    public operator fun minus(o: BPM): BPM = BPM(value - o.value)
    public operator fun times(k: Double): BPM = BPM(value * k)

    public companion object {
        public val CODEC: Codec<BPM> =
            Codec.DOUBLE.xmap(
                ::BPM,
                BPM::value,
            )
        public val STREAM_CODEC: StreamCodec<ByteBuf, BPM> =
            ByteBufCodecs.DOUBLE.map(
                ::BPM,
                BPM::value,
            )
    }
}
