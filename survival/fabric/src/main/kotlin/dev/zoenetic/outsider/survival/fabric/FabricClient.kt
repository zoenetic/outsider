package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.vitals.client.HEARTBEAT
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

public object FabricClient : ClientModInitializer {

    override fun onInitializeClient() {
        Survival.LOGGER.info("Outsider: Survival client starting on Fabric")

        ClientTickEvents.END_CLIENT_TICK.register {
            HEARTBEAT.tick()
        }
    }
}
