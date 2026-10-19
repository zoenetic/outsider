package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFire
import dev.zoenetic.outsider.survival.work.Work
import io.netty.handler.codec.DecoderException
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier

public object OutsiderWork {
    private val all: MutableList<Work<*>> = mutableListOf()

    public val LIGHTING_FIRE: LightingFire = register(
        LightingFire(
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "lighting_fire"),
        ),
    )

    public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Work<*>> =
        Identifier.STREAM_CODEC.cast<RegistryFriendlyByteBuf>().map(
            { id ->
                all.firstOrNull { it.id == id } ?: throw DecoderException("Unknown work: $id")
            },
            Work<*>::id,
        )

    private fun <W : Work<*>> register(work: W): W {
        all.add(work)
        return work
    }
}
