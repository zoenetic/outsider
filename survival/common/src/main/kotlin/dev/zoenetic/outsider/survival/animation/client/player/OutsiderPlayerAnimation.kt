package dev.zoenetic.outsider.survival.animation.client.player

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState

public interface OutsiderPlayerAnimation {
    public fun bodyRotation(strength: Float, state: LivingEntityRenderState): Float
    public fun firstPerson(strength: Float, poseStack: PoseStack, ticks: Float)
}
