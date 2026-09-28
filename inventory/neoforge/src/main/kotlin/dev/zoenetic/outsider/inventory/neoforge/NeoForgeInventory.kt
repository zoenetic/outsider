package dev.zoenetic.outsider.inventory.neoforge

import dev.zoenetic.outsider.inventory.Inventory
import net.minecraft.server.level.ServerLevel
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.tick.LevelTickEvent

@Mod(Inventory.MOD_ID)
public class NeoForgeInventory(modBus: IEventBus) {
    init {
        Inventory.init(NeoForgePlatform)

        NeoForgePlatform.init(modBus)

        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post::class.java) { event ->
            val level = event.level
            if (level !is ServerLevel) return@addListener
            Inventory.tick(level)
        }
    }
}
