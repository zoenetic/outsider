package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimations
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimations
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import dev.zoenetic.outsider.survival.vitals.client.ShiveringAnimation
import dev.zoenetic.outsider.survival.vitals.shiverStrength
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.client.HandDrillAnimation

public object SurvivalClient {
    public fun init() {
        OutsiderItemUseAnimations.register(OutsiderItems.HAND_DRILL, HandDrillAnimation())
        OutsiderPlayerAnimations.register(::shiverStrength, ShiveringAnimation())
    }
}
