package dev.zoenetic.unbidden.fx.fabric

import dev.zoenetic.unbidden.fx.Fx
import net.fabricmc.api.ClientModInitializer

public object FabricFxClient : ClientModInitializer {

    override fun onInitializeClient() {
        Fx.initClient("Fabric")
    }
}
