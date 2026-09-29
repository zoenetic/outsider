package dev.zoenetic.unbidden.fx.neoforge

import dev.zoenetic.unbidden.fx.Fx
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(Fx.MOD_ID)
public class NeoForgeFx(modBus: IEventBus) {
    init {
        Fx.init("NeoForge")
    }
}
