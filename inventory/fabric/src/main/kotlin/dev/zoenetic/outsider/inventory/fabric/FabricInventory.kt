package dev.zoenetic.outsider.inventory.fabric

import dev.zoenetic.outsider.inventory.Inventory
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

public object FabricInventory : ModInitializer {

    override fun onInitialize() {
        Inventory.init(FabricPlatform)

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            Inventory.tick(level)
        }
    }
}
