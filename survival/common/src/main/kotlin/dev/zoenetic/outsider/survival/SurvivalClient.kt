package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.vitals.client.HeartbeatSounds.Companion.HEARTBEAT_SOUNDS
import dev.zoenetic.outsider.survival.vitals.client.ShiveringAnimation
import dev.zoenetic.outsider.survival.vitals.shiverStrength
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.client.HandDrillAnimation
import net.minecraft.client.Minecraft

public object SurvivalClient {
    public fun init() {
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
