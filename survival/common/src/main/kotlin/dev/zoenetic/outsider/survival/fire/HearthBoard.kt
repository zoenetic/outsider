package dev.zoenetic.outsider.survival.fire

import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

public object HearthBoard {

    // how far ahead of the body the spindle starts in player model px
    public const val SPINDLE_PIXELS: Float = 7F

    // players are drawn at 15/16 scale
    private const val FORWARD: Double = SPINDLE_PIXELS / 16.0 * 0.9375

    public fun position(player: Player, partialTick: Float = 1F): Vec3 =
        ahead(player.getPosition(partialTick), player.getPreciseBodyRotation(partialTick))

    public fun inFrontOf(player: Player): Vec3 = ahead(player.position(), player.yRot)

    private fun ahead(feet: Vec3, yawDegrees: Float): Vec3 {
        val yaw = Math.toRadians(yawDegrees.toDouble())
        return feet.add(-sin(yaw) * FORWARD, 0.0, cos(yaw) * FORWARD)
    }
}
