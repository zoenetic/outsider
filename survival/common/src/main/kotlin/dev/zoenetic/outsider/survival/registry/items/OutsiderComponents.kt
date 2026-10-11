package dev.zoenetic.outsider.survival.registry.items

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.getValue
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.Unit

public object OutsiderComponents {

    public val DRILL_TIME_REQUIRED: DataComponentType<Int> by
        Survival.platform.register.component(
            "drill_time_required",
            DataComponentType.Builder<Int>()
                .persistent(ExtraCodecs.POSITIVE_INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT)
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
