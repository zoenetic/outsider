package dev.zoenetic.outsider.survival.neoforge.client

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.SurvivalClient
import dev.zoenetic.outsider.survival.fire.client.EmberHeat
import dev.zoenetic.outsider.survival.fire.client.InLightingPhase
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent
import net.neoforged.neoforge.common.NeoForge

// client only
@Mod(value = Survival.MOD_ID, dist = [Dist.CLIENT])
public class NeoForgeSurvivalClient(modBus: IEventBus) {

    init {
        Survival.LOGGER.info("Outsider: Survival client starting on NeoForge")

        modBus.addListener(FMLClientSetupEvent::class.java) { event ->
            event.enqueueWork { SurvivalClient.init(NeoForgeClientPlatform) }
        }

        modBus.addListener(RegisterConditionalItemModelPropertyEvent::class.java) { event ->
            event.register(InLightingPhase.ID, InLightingPhase.MAP_CODEC)
        }

        modBus.addListener(RegisterRangeSelectItemModelPropertyEvent::class.java) { event ->
            event.register(EmberHeat.ID, EmberHeat.MAP_CODEC)
        }

        val clientBus = NeoForge.EVENT_BUS

        clientBus.addListener(ClientTickEvent.Post::class.java) { event ->
            SurvivalClient.tick()
        }
    }
}
