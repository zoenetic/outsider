package dev.zoenetic.outsider.survival.animation.client.player

import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDefinition
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.PartDefinition
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.core.Direction
import java.util.EnumSet

public const val KNEE_Y: Float = 6F
public const val KNEE_FRONT: Float = -2F

private const val HIP_Y: Float = 12F
private const val LEG_LENGTH: Float = 12F
private const val LEG_DEPTH: Float = 4F
private const val SHIN_LENGTH: Float = LEG_LENGTH - KNEE_Y

private const val PANTS_GROWTH: Float = 0.25F
private const val SHIN: String = "shin"
private const val SHIN_PANTS: String = "shin_pants"
private const val SIDE_INSET: Float = 0.01F

private val NO_CUT_END: Set<Direction> = EnumSet.complementOf(EnumSet.of(Direction.UP))
private val SIDES_ONLY: Set<Direction> = EnumSet.complementOf(
    EnumSet.of(Direction.UP, Direction.DOWN),
)
private val SOLE_ONLY: Set<Direction> = EnumSet.of(Direction.UP)

private enum class LegSide(
    val id: String,
    val x: Float,
    val skinU: Int,
    val skinV: Int,
    val pantsU: Int,
    val pantsV: Int,
) {
    LEFT("left", 1.9F, 16, 48, 0, 48),
    RIGHT("right", -1.9F, 0, 16, 0, 32),
}

// client only
public fun splitLegs(root: PartDefinition, scale: CubeDeformation) {
    for (side in LegSide.entries) splitLeg(root, side, scale)
}

private fun splitLeg(root: PartDefinition, side: LegSide, scale: CubeDeformation) {
    val pants: CubeDeformation = scale.extend(PANTS_GROWTH)
    val thigh: PartDefinition = root.addOrReplaceChild(
        "${side.id}_leg",
        cubes(legBox(side.skinU, side.skinV, 0F, KNEE_Y, KNEE_FRONT, scale, NO_CUT_END)),
        PartPose.offset(side.x, HIP_Y, 0F),
    )
    thigh.addOrReplaceChild(
        "${side.id}_pants",
        cubes(legBox(side.pantsU, side.pantsV, 0F, KNEE_Y, KNEE_FRONT, pants, NO_CUT_END)),
        PartPose.ZERO,
    )
    val shin: PartDefinition = thigh.addOrReplaceChild(
        SHIN,
        cubes(
            legBox(side.skinU, side.skinV + 6, 0F, SHIN_LENGTH, 0F, inset(scale), SIDES_ONLY),
            legBox(side.skinU, side.skinV, SHIN_LENGTH, 0F, 0F, scale, SOLE_ONLY),
        ),
        PartPose.offset(0F, KNEE_Y, KNEE_FRONT),
    )
    shin.addOrReplaceChild(
        SHIN_PANTS,
        cubes(
            legBox(
                side.pantsU,
                side.pantsV + 6,
                0F,
                SHIN_LENGTH,
                0F,
                inset(pants),
                SIDES_ONLY,
            ),
            legBox(side.pantsU, side.pantsV, SHIN_LENGTH, 0F, 0F, pants, SOLE_ONLY),
        ),
        PartPose.ZERO,
    )
}

private fun inset(grow: CubeDeformation): CubeDeformation = grow.extend(-SIDE_INSET, 0F, 0F)

private fun legBox(
    u: Int,
    v: Int,
    y: Float,
    height: Float,
    z: Float,
    grow: CubeDeformation,
    faces: Set<Direction>,
): CubeDefinition = CubeDefinition(
    null,
    u.toFloat(),
    v.toFloat(),
    -2F,
    y,
    z,
    4F,
    height,
    LEG_DEPTH,
    grow,
    false,
    1F,
    1F,
    faces,
)

private class CubeList(private val cubes: List<CubeDefinition>) : CubeListBuilder() {
    override fun getCubes(): List<CubeDefinition> = cubes
}

private fun cubes(vararg cubes: CubeDefinition): CubeListBuilder = CubeList(cubes.toList())

public fun shinOf(leg: ModelPart): ModelPart? = if (leg.hasChild(SHIN)) leg.getChild(SHIN) else null

public fun syncShinPants(model: PlayerModel) {
    shinOf(model.leftLeg)?.getChild(SHIN_PANTS)?.visible = model.leftPants.visible
    shinOf(model.rightLeg)?.getChild(SHIN_PANTS)?.visible = model.rightPants.visible
}
