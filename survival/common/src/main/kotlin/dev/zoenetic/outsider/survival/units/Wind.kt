package dev.zoenetic.outsider.survival.units

import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.DensityFunction
import net.minecraft.world.phys.Vec3

public val CALM: Wind = Wind(Vec3(0.0, 0.0, 0.0))

public const val GRADIENT_STEP: Int = 16
public const val SPEED_SCALE: Double = 1000.0
public const val MAX_SPEED: Double = 15.0
public const val CHILL: Double = 0.2

public data class Wind(public val vector: Vec3) {
    public val speed: Double = vector.length()
    public val conductance: Conductance = Conductance(1.0 + CHILL * speed)

    public companion object {
        public fun fromDensityFunction(function: DensityFunction, chunk: LevelChunk): Wind {
            val gradient = function.horizontalGradient(
                chunk.pos.middleBlockX,
                chunk.level.seaLevel,
                chunk.pos.middleBlockZ,
                GRADIENT_STEP,
            )
            val slope = gradient.horizontalDistance()
            if (slope < 1e-9) return CALM
            val speed = (slope * SPEED_SCALE).coerceAtMost(MAX_SPEED)
            return Wind(gradient.normalize().scale(speed))
        }
    }
}

public fun DensityFunction.horizontalGradient(x: Int, y: Int, z: Int, h: Int): Vec3 {
    val dx = compute(DensityFunction.SinglePointContext(x + h, y, z)) -
        compute(DensityFunction.SinglePointContext(x - h, y, z))
    val dz = compute(DensityFunction.SinglePointContext(x, y, z + h)) -
        compute(DensityFunction.SinglePointContext(x, y, z - h))
    return Vec3(dx / (2.0 * h), 0.0, dz / (2.0 * h))
}
