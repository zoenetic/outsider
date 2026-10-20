package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.Survival
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

public object WorkClosePacketPayload : CustomPacketPayload {
    public val TYPE: CustomPacketPayload.Type<WorkClosePacketPayload> =
        CustomPacketPayload.Type(
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "close_work"),
        )

    public val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, WorkClosePacketPayload> =
        StreamCodec.unit(
            this,
        )

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
