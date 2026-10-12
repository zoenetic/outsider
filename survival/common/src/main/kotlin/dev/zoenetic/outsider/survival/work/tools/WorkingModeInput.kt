package dev.zoenetic.outsider.survival.work.tools

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Input

public object WorkingModeInput {
    @JvmStatic
    public fun isLocked(): Boolean =
        Minecraft.getInstance().player?.workingBlockTargetOrNull() != null

    @JvmStatic
    public fun lock(input: Input): Input =
        Input(false, false, false, false, false, input.shift(), false)
}
