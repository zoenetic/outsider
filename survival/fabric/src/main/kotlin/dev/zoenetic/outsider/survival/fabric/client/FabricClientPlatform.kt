package dev.zoenetic.outsider.survival.fabric.client

import dev.zoenetic.outsider.survival.platform.client.ClientPlatform
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

public object FabricClientPlatform : ClientPlatform {
    override fun sendToServer(packet: CustomPacketPayload) {
        ClientPlayNetworking.send(packet)
    }
}
