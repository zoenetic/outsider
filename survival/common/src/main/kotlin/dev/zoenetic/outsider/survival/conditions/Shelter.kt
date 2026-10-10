package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.units.Wind
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import kotlin.math.cos

internal const val SHELTER_HALF_ANGLE_DEGREES: Double = 45.0
internal const val SHELTER_RAYS_PER_SIDE: Int = 2
internal const val SHELTER_RAY_LENGTH: Double = 12.0

public fun Player.getWindExposure(wind: Wind): Double {
    if (wind.speed < Mth.EPSILON) return 1.0
    val upwind = wind.vector.scale(-1.0 / wind.speed)
    val halfAngle = Math.toRadians(SHELTER_HALF_ANGLE_DEGREES)
    val origin = eyePosition
    var totalWeight = 0.0
    var blockedWeight = 0.0
    for (i in -SHELTER_RAYS_PER_SIDE..SHELTER_RAYS_PER_SIDE) {
        val theta = halfAngle * i / SHELTER_RAYS_PER_SIDE
        val direction = upwind.yRot(theta.toFloat())
        val weight = cos(theta)
        val hit = level().clip(
            ClipContext(
                origin,
                origin.add(direction.scale(SHELTER_RAY_LENGTH)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this,
            ),
        )
        totalWeight += weight
        if (hit.type == HitResult.Type.BLOCK) blockedWeight += weight
    }
    return 1.0 - blockedWeight / totalWeight
}

public fun Wind.sheltered(exposure: Double): Wind =
    if (exposure >= 1.0) this else Wind(vector.scale(exposure))
