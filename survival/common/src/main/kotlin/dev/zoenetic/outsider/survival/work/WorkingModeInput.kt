package dev.zoenetic.outsider.survival.work

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Input

public object WorkingModeInput {
    @JvmStatic
    public fun isLocked(): Boolean = Minecraft.getInstance().player.let {
        it != null && Work.get(it) != null
    }

    @JvmStatic
    public fun lock(input: Input): Input =
        Input(false, false, false, false, false, input.shift(), false)
}
