package dev.zoenetic.outsider.survival.registry

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.platform.getValue
import net.minecraft.core.component.DataComponentType
import net.minecraft.util.Unit

public object OutsiderComponents {

    public val FUEL_LEVEL: DataComponentType<Fuel> by
    Survival.platform.register.component(
        "fuel_level", DataComponentType.Builder<Fuel>()
            .persistent(Fuel.CODEC)
            .networkSynchronized(Fuel.STREAM_CODEC)
            .ignoreSwapAnimation()
    )

    public val LIT: DataComponentType<Unit> by
    Survival.platform.register.component(
        "lit", DataComponentType.Builder<Unit>()
            .persistent(Unit.CODEC)
            .networkSynchronized(Unit.STREAM_CODEC)
            .ignoreSwapAnimation()
    )

    public fun init() {}

}