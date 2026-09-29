package dev.zoenetic.unbidden.fx.fabric

import dev.zoenetic.unbidden.fx.Fx
import net.fabricmc.api.ModInitializer

public object FabricFx : ModInitializer {

    override fun onInitialize() {
        Fx.init("Fabric")
    }
}
