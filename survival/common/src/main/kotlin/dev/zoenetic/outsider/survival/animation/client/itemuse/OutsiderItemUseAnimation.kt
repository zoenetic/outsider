package dev.zoenetic.outsider.survival.animation.client.itemuse

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.world.entity.HumanoidArm

public interface OutsiderItemUseAnimation {
    public fun firstPerson(
        poseStack: PoseStack,
        arm: HumanoidArm,
        inverseArmHeight: Float,
        ticks: Float,
    )

    public fun thirdPerson(rightArm: ModelPart, leftArm: ModelPart, ticks: Float)
}
