package dev.zoenetic.unbidden.fx.neoforge

import dev.zoenetic.unbidden.fx.Fx
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(value = Fx.MOD_ID, dist = [Dist.CLIENT])
public class NeoForgeFxClient(modBus: IEventBus) {
    init {
        Fx.initClient("NeoForge")
    }
}
