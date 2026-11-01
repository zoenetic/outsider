package dev.zoenetic.outsider.survival.animation.client.player

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState

public interface OutsiderPlayerAnimation {
    public fun bodyRotation(strength: Float, state: LivingEntityRenderState): Float
    public fun firstPerson(strength: Float, poseStack: PoseStack, ticks: Float)
    public fun thirdPerson(strength: Float, model: HumanoidModel<*>, state: HumanoidRenderState) {}
}
