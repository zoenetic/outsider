package dev.zoenetic.outsider.survival.animation.client.player

import net.minecraft.world.entity.LivingEntity

public object OutsiderPlayerAnimations {

    private val all: MutableMap<(LivingEntity) -> Float, OutsiderPlayerAnimation> =
        mutableMapOf()

    public fun of(entity: LivingEntity): Map<OutsiderPlayerAnimation, Float> =
        all.entries.associate { (function, animation) ->
            val strength = function.invoke(entity)
            animation to strength
        }.filter { (_, strength) -> strength > 0f }

    public fun register(function: (LivingEntity) -> Float, animation: OutsiderPlayerAnimation) {
        all[function] = animation
    }
}
