package dev.zoenetic.outsider.inventory.fabric

import dev.zoenetic.outsider.inventory.platform.Platform
import net.fabricmc.loader.api.FabricLoader

public object FabricPlatform : Platform {
    override val name: String = "fabric"
    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    override val register: FabricRegister = FabricRegister
}
