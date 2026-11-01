package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.component.DataComponentType
import net.minecraft.util.Unit

public object OutsiderComponents {

    public val BURNS_OUT_AT: DataComponentType<Time> by
        Survival.platform.register.component(
            "burns_out_at",
            DataComponentType.Builder<Time>()
                .persistent(Time.CODEC)
                .networkSynchronized(Time.STREAM_CODEC)
                .ignoreSwapAnimation(),
        )

    public val LIT: DataComponentType<Unit> by
        Survival.platform.register.component(
            "lit",
            DataComponentType.Builder<Unit>()
                .persistent(Unit.CODEC)
                .networkSynchronized(Unit.STREAM_CODEC)
                .ignoreSwapAnimation(),
        )

    public fun init(): kotlin.Unit = kotlin.Unit
}
