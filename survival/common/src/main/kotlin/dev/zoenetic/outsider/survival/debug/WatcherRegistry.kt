package dev.zoenetic.outsider.survival.debug

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.climate.getWind
import dev.zoenetic.outsider.survival.emission.EmitterIndex.heatAtPlayer
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.Exertion
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import java.util.*

public object WatcherRegistry {

    public val registry: MutableSet<UUID> = mutableSetOf()

    public fun add(uuid: UUID) {
        registry.add(uuid)
    }

    public fun addDev(player: ServerPlayer) {
        if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
            && Survival.platform.isDevelopmentEnvironment
        ) add(player.uuid)
    }

    public fun remove(uuid: UUID) {
        registry.remove(uuid)
    }

    public fun tick(server: MinecraftServer) {
        val players = server.playerList.playersByUUID
        for (uuid in registry) {
            if (players.contains(uuid)) {
                val player = players[uuid] ?: continue
                val level = player.level()
                val time = Time(level.gameTime)
                val conditions = Survival.platform.playerConditions.get(player) ?: continue
                val vitals = Survival.platform.vitals.get(player) ?: continue
                val exertion = Exertion.average(player.uuid)
                val speed = player.getAttributeValue(MOVEMENT_SPEED)
                val elapsed: Duration = time - conditions.time
                if (elapsed.value == 0L) {
                    val radiant = heatAtPlayer(player)
                    val chunkWind = level.getChunkAt(player.blockPosition()).getWind()
                    player.sendSystemMessage(
                        Component.literal(
                            "FL${"%.1f".format(conditions.feelsLike().celsius)}" +
                                    " A${"%.1f".format(conditions.ambient.celsius)}" +
                                    " H${"%.1f".format(conditions.radiant.celsius)}/${
                                        "%.1f".format(
                                            radiant.celsius
                                        )
                                    }" +
                                    " W${"%.1f".format(conditions.wind.speed)}/${
                                        "%.1f".format(
                                            chunkWind.speed
                                        )
                                    }" +
                                    " X${"%.2f".format(conditions.windExposure)}" +
                                    " RH${"%.2f".format(conditions.humidity.value)}" +
                                    " S${"%.1f".format(conditions.sky.value)}${if (conditions.isUnderOpenSky) "o" else "c"}" +
                                    " | B${"%.1f".format(vitals.bodyTemperature.heat.celsius)}" +
                                    " R${"%.0f".format(vitals.breathingRate.value)}" +
                                    " P${"%.0f".format(vitals.heartRate.bpm.value)}" +
                                    " V${"%.0f".format(speed * 100)}" +
                                    " E${"%.1f".format(exertion?.value)}"
                        ), true
                    )
                }
            }
        }
    }
}