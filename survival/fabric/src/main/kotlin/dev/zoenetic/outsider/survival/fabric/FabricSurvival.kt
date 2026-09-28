package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.ServerState
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.debug.*
import dev.zoenetic.outsider.survival.emission.EmitterIndex.reconcileEmitters
import dev.zoenetic.outsider.survival.vitals.Exertion
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

public object FabricSurvival : ModInitializer {

    override fun onInitialize() {
        Survival.init(FabricPlatform)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                rootCommand.then(
                    survivalCommand
                        .then(setBodyTemperatureCommand)
                        .then(watchCommand)
                )
            )
        }

        ServerLifecycleEvents.SERVER_STARTING.register { ServerState.onServerStarting() }
        ServerLifecycleEvents.SERVER_STOPPED.register { ServerState.onServerStopped() }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            WatcherRegistry.addDev(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            Exertion.remove(handler.player.uuid)
            WatcherRegistry.remove(handler.player.uuid)
        }

        ServerChunkEvents.CHUNK_LOAD.register { level, chunk, _ ->
            ServerState.dropSchedule(level).reset(chunk.pos, chunk.reconcileEmitters())
        }

        ServerChunkEvents.CHUNK_UNLOAD.register { level, chunk ->
            ServerState.dropSchedule(level).reset(chunk.pos, null)
        }

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            Survival.tick(level)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            WatcherRegistry.tick(server)
        }
    }
}
