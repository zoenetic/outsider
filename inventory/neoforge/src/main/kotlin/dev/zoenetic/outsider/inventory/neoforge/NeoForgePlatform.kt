package dev.zoenetic.outsider.inventory.neoforge

import dev.zoenetic.outsider.inventory.platform.Platform
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.loading.FMLLoader

public object NeoForgePlatform : Platform {
    override val name: String = "neoforge"

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.getCurrent().isProduction

    override fun isModLoaded(modId: String): Boolean =
        FMLLoader.getCurrent().getLoadingModList()
            .getModFileById(modId) != null

    override val register: NeoForgeRegister = NeoForgeRegister

    public fun init(bus: IEventBus) {
        register.init(bus)
    }
}
