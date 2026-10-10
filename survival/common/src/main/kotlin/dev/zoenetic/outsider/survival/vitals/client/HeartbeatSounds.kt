package dev.zoenetic.outsider.survival.vitals.client

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.Heartbeat
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundEngine.PlayResult

public val HEARTBEAT: HeartbeatSounds = HeartbeatSounds.init()

// client only
public data class HeartbeatSounds(var lastPlayer: LocalPlayer?, var lastHeartbeatTime: Time?) {
    public fun beat(heartbeat: Heartbeat, gameTime: Time) {
        val pitch = heartbeat.pitch
        val volume = heartbeat.volume
        val instance =
            SimpleSoundInstance.forUI(OutsiderSounds.HEARTBEAT, pitch, volume)
        val result = Minecraft.getInstance().soundManager.play(instance)
        if (result == PlayResult.NOT_STARTED) {
            Survival.LOGGER.debug("heart beat sound not started")
        }
        lastHeartbeatTime = gameTime
    }

    public fun player(): LocalPlayer? = Minecraft.getInstance().player

    public fun tick() {
        val level = Minecraft.getInstance().level ?: return
        val gameTime = Time(level.gameTime)
        val player = player() ?: return
        if (lastPlayer != player) {
            this.reset(player)
            return
        }
        val vitals = Survival.platform.vitals.get(player) ?: return
        val heartRate = vitals.heartRate
        if (heartRate.volume() <= 0F) return
        val heartbeat = heartRate.toHeartbeat() ?: return
        val interval = heartbeat.interval
        val last = lastHeartbeatTime
        if (last != null) {
            val elapsed = gameTime - last
            if (elapsed < interval) return
        }
        beat(heartbeat, gameTime)
    }

    public fun reset(newPlayer: LocalPlayer) {
        lastPlayer = newPlayer
        lastHeartbeatTime = null
    }

    public companion object {
        public fun init(): HeartbeatSounds = HeartbeatSounds(
            null,
            null,
        )
    }
}
