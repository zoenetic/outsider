package dev.zoenetic.outsider.survival.work

import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

private const val LOOK_LIMIT: Float = 90F

public fun clampWorkingLook(player: Player) {
    val anchor: Vec3 = Work.get(player)?.anchor() ?: return
    val body: Float = yawTowards(player.position(), anchor)
    val turned: Float = Mth.wrapDegrees(player.yRot - body)
    val correction: Float = Mth.clamp(turned, -LOOK_LIMIT, LOOK_LIMIT) - turned
    player.yRotO += correction
    player.yRot += correction
    player.yHeadRot = player.yRot
}
