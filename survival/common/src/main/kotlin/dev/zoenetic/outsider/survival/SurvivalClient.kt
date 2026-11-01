package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations
import dev.zoenetic.outsider.survival.fire.client.BlowingAnimation
import dev.zoenetic.outsider.survival.fire.client.carryHoldIntoBlowing
import dev.zoenetic.outsider.survival.fire.client.litTinderParticles
import dev.zoenetic.outsider.survival.platform.client.ClientPlatform
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.tools.drill.handdrill.client.HandDrillAnimation
import dev.zoenetic.outsider.survival.vitals.client.HeartbeatSounds.Companion.HEARTBEAT_SOUNDS
import dev.zoenetic.outsider.survival.vitals.client.ShiveringAnimation
import dev.zoenetic.outsider.survival.vitals.shiverStrength
import dev.zoenetic.outsider.survival.work.Work
import dev.zoenetic.outsider.survival.work.client.KneelingAnimation
import dev.zoenetic.outsider.survival.work.client.workingStrength
import net.minecraft.client.Minecraft

public object SurvivalClient {

    public lateinit var platform: ClientPlatform
        private set

    public fun init(platform: ClientPlatform) {
        this.platform = platform
        OutsiderItemUseAnimations.register(OutsiderItems.HAND_DRILL, HandDrillAnimation())
        OutsiderItemUseAnimations.register(OutsiderItems.TINDER, BlowingAnimation())
        OutsiderPlayerAnimations.register(::workingStrength, KneelingAnimation())
        OutsiderPlayerAnimations.register(::shiverStrength, ShiveringAnimation())
    }

    public fun tick() {
        val minecraft = Minecraft.getInstance()
        val level = minecraft.level ?: return
        if (minecraft.isPaused) return
        Work.tick(level)
        carryHoldIntoBlowing(minecraft)
        litTinderParticles(minecraft, level)
        HEARTBEAT_SOUNDS.tick()
    }
}
