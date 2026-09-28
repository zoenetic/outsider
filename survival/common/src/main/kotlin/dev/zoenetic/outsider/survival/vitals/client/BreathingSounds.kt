package dev.zoenetic.outsider.survival.vitals.client

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.OutsiderSounds
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.Breath
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundEngine.PlayResult

public val BREATHING: BreathSounds = BreathSounds.init()

// client only
public data class BreathSounds(
    var lastPlayer: LocalPlayer?,
    var lastBreathTime: Time?,
) {
    internal fun playBreathSound(breath: Breath, gameTime: Time) {
        val pitch = breath.pitch
        val volume = breath.volume
        val instance = SimpleSoundInstance.forUI(OutsiderSounds.BREATH, pitch, volume)
        val result = Minecraft.getInstance().soundManager.play(instance)
        if (result == PlayResult.NOT_STARTED) {
            Survival.LOGGER.debug("breath sound not started")
        }
        lastBreathTime = gameTime
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
        val breathing = vitals.breathingRate
        if (breathing.volume() <= 0F) return
        val breath = breathing.toBreath() ?: return
        val interval = breath.interval
        val last = lastBreathTime
        if (last != null) {
            val elapsed = gameTime - last
            if (elapsed < interval) return
        }
        playBreathSound(breath, gameTime)
    }

    public fun reset(newPlayer: LocalPlayer) {
        lastPlayer = newPlayer
        lastBreathTime = null
    }

    public companion object {
        public fun init(): BreathSounds {
            return BreathSounds(
                null, null
            )
        }
    }
}