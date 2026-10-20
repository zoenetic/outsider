package dev.zoenetic.outsider.survival.work

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Input

public fun maybeMoveOutOfWorkMode(player: ServerPlayer, old: Input, new: Input) {
    val _ = Work.get(player) ?: return
    if (
        (new.left && !old.left) ||
        (new.right && !old.right) ||
        (new.forward && !old.forward) ||
        (new.backward && !old.backward) ||
        (new.jump && !old.jump)
    ) {
        Work.end(player)
    }
}
