package dev.zoenetic.outsider.survival.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererInvoker {
    @Invoker("renderPlayerArm")
    void outsider_survival$renderPlayerArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                        int lightCoords, float inverseArmHeight, float attackValue, HumanoidArm arm);
}
