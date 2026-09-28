package dev.zoenetic.outsider.survival.neoforge

import dev.zoenetic.outsider.survival.ServerState
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.debug.*
import dev.zoenetic.outsider.survival.emission.EmitterIndex.reconcileEmitters
import dev.zoenetic.outsider.survival.vitals.Exertion
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent

@Mod(Survival.MOD_ID)
public class NeoForgeSurvival(modBus: IEventBus) {
    init {
        Survival.init(NeoForgePlatform)

        NeoForgePlatform.init(modBus)

        val eventBus = NeoForge.EVENT_BUS

        eventBus.addListener(RegisterCommandsEvent::class.java) { event ->
            event.dispatcher.register(
                rootCommand.then(
                    survivalCommand
                        .then(setBodyTemperatureCommand)
                        .then(watchCommand)
                )
            )
        }

        eventBus.addListener(ServerAboutToStartEvent::class.java) { _ ->
            ServerState.onServerStarting()
        }

        eventBus.addListener(ServerStoppedEvent::class.java) { _ ->
            ServerState.onServerStopped()
        }

        eventBus.addListener(PlayerEvent.PlayerLoggedInEvent::class.java) { event ->
            val player = event.entity as? ServerPlayer ?: return@addListener
            WatcherRegistry.addDev(player)
        }

        eventBus.addListener(PlayerEvent.PlayerLoggedOutEvent::class.java) { event ->
            val player = event.entity as? ServerPlayer ?: return@addListener
            Exertion.remove(player.uuid)
            WatcherRegistry.remove(player.uuid)
        }

        eventBus.addListener(ChunkEvent.Load::class.java) { event ->
            val chunk = event.chunk
            ServerState.dropSchedule(chunk.level)
                .reset(chunk.pos, chunk.reconcileEmitters())
        }

        eventBus.addListener(ChunkEvent.Unload::class.java) { event ->
            val chunk = event.chunk
            ServerState.dropSchedule(chunk.level).reset(chunk.pos, null)
        }

        eventBus.addListener(LevelTickEvent.Post::class.java) { event ->
            val level = event.level
            if (level !is ServerLevel) return@addListener
            Survival.tick(level)
        }

        eventBus.addListener(ServerTickEvent.Post::class.java) { event ->
            WatcherRegistry.tick(event.server)
        }
    }
}
