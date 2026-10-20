package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations
import dev.zoenetic.outsider.survival.platform.client.ClientPlatform
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.tools.drill.handdrill.client.HandDrillAnimation
import dev.zoenetic.outsider.survival.vitals.client.HeartbeatSounds.Companion.HEARTBEAT_SOUNDS
import dev.zoenetic.outsider.survival.vitals.client.ShiveringAnimation
import dev.zoenetic.outsider.survival.vitals.shiverStrength
import dev.zoenetic.outsider.survival.work.Work
import net.minecraft.client.Minecraft

public object SurvivalClient {

    public lateinit var platform: ClientPlatform
        private set

    public fun init(platform: ClientPlatform) {
        this.platform = platform
        OutsiderItemUseAnimations.register(OutsiderItems.HAND_DRILL, HandDrillAnimation())
        OutsiderPlayerAnimations.register(::shiverStrength, ShiveringAnimation())
    }

    public fun tick() {
        val minecraft = Minecraft.getInstance()
        val level = minecraft.level ?: return
        if (minecraft.isPaused) return
        Work.tick(level)
        HEARTBEAT_SOUNDS.tick()
    }
}
