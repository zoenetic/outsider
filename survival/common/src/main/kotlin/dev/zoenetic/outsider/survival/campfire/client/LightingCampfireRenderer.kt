package dev.zoenetic.outsider.survival.campfire.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.outsider.survival.fire.client.ClientFireAttempt
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.LivingEntity
import kotlin.math.PI
import kotlin.math.sin

public interface LightingCampfireState {
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    public fun `outsider_survival$getLightingCampfire`(): Boolean

    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    public fun `outsider_survival$setLightingCampfire`(state: Boolean)
}

public const val LIGHTING_STROKES_PER_SECOND: Double = 3.0
public const val LIGHTING_STROKE_LENGTH: Float = 0.08F
public const val LIGHTING_ARM_SLIDE: Float = 2.0F

// client only
public object LightingCampfireRenderer {

    private var player: LocalPlayer? = null
    private var frameInterp: Float = 0F

    public fun beginFrame(player: LocalPlayer, frameInterp: Float) {
        this.player = player
        this.frameInterp = frameInterp
    }

    public fun isLighting(player: LocalPlayer): Boolean =
        with(ClientFireAttempt) { player.isAttemptingToLightAFire() }

    private fun phaseAt(ticks: Float): Float =
        ticks * (2.0 * PI * LIGHTING_STROKES_PER_SECOND / 20.0).toFloat()

    public fun applyToArm(poseStack: PoseStack, arm: HumanoidArm, inverseArmHeight: Float) {
        val player = this.player ?: return
        if (!isLighting(player)) return

        val sign = if (arm == HumanoidArm.RIGHT) 1F else -1F
        val stroke = sin(phaseAt(player.tickCount + frameInterp)) * LIGHTING_STROKE_LENGTH

        val anchorX = sign * 0.64F
        val anchorY = -0.6F + inverseArmHeight * -0.6F
        val anchorZ = -0.72F

        val targetX = sign * 0.35F
        val targetY = -0.35F
        val targetZ = -0.72F + sign * stroke

        poseStack.translate(targetX, targetY, targetZ)
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * 55F))
        poseStack.translate(-anchorX, -anchorY, -anchorZ)
    }

    public fun capture(entity: LivingEntity, state: LivingEntityRenderState) {
        val lighting = entity is LocalPlayer && isLighting(entity)
        (state as LightingCampfireState).`outsider_survival$setLightingCampfire`(lighting)
    }

    public fun applyToModel(
        rightArm: ModelPart,
        leftArm: ModelPart,
        state: LivingEntityRenderState,
    ) {
        if (!(state as LightingCampfireState).`outsider_survival$getLightingCampfire`()) return

        val slide = sin(phaseAt(state.ageInTicks)) * LIGHTING_ARM_SLIDE

        rightArm.xRot = -1.3F
        rightArm.yRot = -0.55F
        rightArm.zRot = 0F
        rightArm.z = slide

        leftArm.xRot = -1.3F
        leftArm.yRot = 0.55F
        leftArm.zRot = 0F
        leftArm.z = -slide
    }
}
