package dev.zoenetic.outsider.survival.fabric.client

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.SurvivalClient
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

public object FabricSurvivalClient : ClientModInitializer {

    override fun onInitializeClient() {
        Survival.LOGGER.info("Outsider: Survival client starting on Fabric")

        SurvivalClient.init(FabricClientPlatform)

        ClientTickEvents.END_CLIENT_TICK.register {
            SurvivalClient.tick()
        }
    }
}
