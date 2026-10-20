package dev.zoenetic.outsider.survival.neoforge.client

import dev.zoenetic.outsider.survival.platform.client.ClientPlatform
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.neoforged.neoforge.client.network.ClientPacketDistributor

public object NeoForgeClientPlatform : ClientPlatform {
    override fun sendToServer(packet: CustomPacketPayload) {
        ClientPacketDistributor.sendToServer(packet)
    }
}
