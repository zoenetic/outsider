package dev.zoenetic.outsider.survival.platform.client

import net.minecraft.network.protocol.common.custom.CustomPacketPayload

public interface ClientPlatform {
    public fun sendToServer(packet: CustomPacketPayload)
}
