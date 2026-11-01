package dev.zoenetic.outsider.survival.animation.client.itemuse

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.HumanoidModel
import net.minecraft.world.entity.HumanoidArm

public interface OutsiderItemUseAnimation {
    public val twoHanded: Boolean get() = false

    public fun firstPerson(
        poseStack: PoseStack,
        arm: HumanoidArm,
        inverseArmHeight: Float,
        ticks: Float,
    )

    public fun firstPersonArm(poseStack: PoseStack, arm: HumanoidArm, ticks: Float) {}

    public fun thirdPerson(model: HumanoidModel<*>, ticks: Float)

    public fun thirdPersonItem(
        poseStack: PoseStack,
        model: HumanoidModel<*>,
        arm: HumanoidArm,
        ticks: Float,
    ) {
    }
}
