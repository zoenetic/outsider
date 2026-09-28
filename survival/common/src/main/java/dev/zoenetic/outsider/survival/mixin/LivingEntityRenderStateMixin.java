package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.campfire.client.LightingCampfireState;
import dev.zoenetic.outsider.survival.vitals.client.ShiverState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ShiverState, LightingCampfireState {
    @Unique
    private double outsider_survival$shiver;
    @Unique
    private boolean outsider_survival$lightingCampfire;

    @Override
    public double outsider_survival$getShiver() {
        return outsider_survival$shiver;
    }

    @Override
    public void outsider_survival$setShiver(double shiver) {
        outsider_survival$shiver = shiver;
    }

    @Override
    public boolean outsider_survival$getLightingCampfire() {
        return outsider_survival$lightingCampfire;
    }

    @Override
    public void outsider_survival$setLightingCampfire(boolean state) {
        outsider_survival$lightingCampfire = state;
    }
}
