package dev.zoenetic.outsider.survival.work

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max

private const val WORK_CLEARANCE = 0.2

public fun yawTowards(from: Vec3, to: Vec3): Float =
    Math.toDegrees(atan2(to.z - from.z, to.x - from.x)).toFloat() - 90F

public fun Player.workingEyePosition(): Vec3 =
    position().add(0.0, getEyeHeight(Pose.CROUCHING).toDouble(), 0.0)

public fun ServerPlayer.centreOn(blockPos: BlockPos) {
    val target = Vec3.atBottomCenterOf(blockPos)
    val offset = Vec3(x - target.x, 0.0, z - target.z)
    val away = if (offset.lengthSqr() > 1e-6) {
        offset.normalize()
    } else {
        Vec3.directionFromRotation(0F, yRot).reverse()
    }
    val reach = (0.5 + bbWidth / 2.0 + WORK_CLEARANCE) / max(abs(away.x), abs(away.z))
    val standAt = Vec3(target.x + away.x * reach, y, target.z + away.z * reach)
    val free = level().noCollision(this, boundingBox.move(standAt.subtract(position())))
    val destination = if (free) standAt else position()
    val shape = level().getBlockState(blockPos).getShape(level(), blockPos)
    val lookAt = if (shape.isEmpty) {
        Vec3.atCenterOf(blockPos)
    } else {
        Vec3.atLowerCornerOf(blockPos).add(shape.bounds().center)
    }
    val look = lookAt.subtract(destination.add(0.0, getEyeHeight(Pose.CROUCHING).toDouble(), 0.0))
    val yaw = yawTowards(destination, lookAt)
    val pitch = -Math.toDegrees(atan2(look.y, look.horizontalDistance())).toFloat()
    connection.teleport(destination.x, destination.y, destination.z, yaw, pitch)
}
