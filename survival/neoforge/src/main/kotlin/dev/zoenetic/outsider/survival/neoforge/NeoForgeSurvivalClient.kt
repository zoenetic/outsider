package dev.zoenetic.outsider.survival.neoforge

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.SurvivalClient
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge

@Mod(value = Survival.MOD_ID, dist = [Dist.CLIENT])
public class NeoForgeSurvivalClient(modBus: IEventBus) {

    init {
        Survival.LOGGER.info("Outsider: Survival client starting on NeoForge")

        modBus.addListener(FMLClientSetupEvent::class.java) { event ->
            event.enqueueWork { SurvivalClient.init() }
        }

        val clientBus = NeoForge.EVENT_BUS

        clientBus.addListener(ClientTickEvent.Post::class.java) { event ->
            SurvivalClient.tick()
        }
    }
}
