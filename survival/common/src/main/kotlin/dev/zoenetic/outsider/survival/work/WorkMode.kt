package dev.zoenetic.outsider.survival.work

import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Input
import net.minecraft.world.entity.player.Player

public object WorkMode {
    @JvmStatic
    public fun isEngaged(): Boolean = Minecraft.getInstance().player.let {
        it != null && Work.get(it) != null
    }

    @JvmStatic
    public fun isEngagedFor(player: Player): Boolean = Work.get(player) != null

    @JvmStatic
    public fun lockShift(input: Input): Input = Input(
        input.forward,
        input.backward,
        input.left,
        input.right,
        input.jump,
        true,
        input.sprint,
    )
}
