package dev.zoenetic.outsider.survival.fabric.client

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.SurvivalClient
import dev.zoenetic.outsider.survival.fire.client.EmberHeat
import dev.zoenetic.outsider.survival.fire.client.InLightingPhase
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties

public object FabricSurvivalClient : ClientModInitializer {

    override fun onInitializeClient() {
        Survival.LOGGER.info("Outsider: Survival client starting on Fabric")

        SurvivalClient.init(FabricClientPlatform)

        ConditionalItemModelProperties.ID_MAPPER.put(InLightingPhase.ID, InLightingPhase.MAP_CODEC)

        RangeSelectItemModelProperties.ID_MAPPER.put(EmberHeat.ID, EmberHeat.MAP_CODEC)

        ClientTickEvents.END_CLIENT_TICK.register {
            SurvivalClient.tick()
        }
    }
}
