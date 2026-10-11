package dev.zoenetic.outsider.survival.mixin;

import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimation;
import dev.zoenetic.outsider.survival.animation.client.itemuse.OutsiderItemUseAnimationState;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimation;
import dev.zoenetic.outsider.survival.animation.client.player.OutsiderPlayerAnimationState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements OutsiderItemUseAnimationState, OutsiderPlayerAnimationState {

    @Unique
    private OutsiderItemUseAnimation outsider_survival$itemUseAnimation;

    @Unique
    private float outsider_survival$itemUseTime;

    @Override
    public OutsiderItemUseAnimation outsider_survival$getItemUseAnimation() {
        return outsider_survival$itemUseAnimation;
    }

    @Override
    public float outsider_survival$getItemUseTicks() {
        return outsider_survival$itemUseTime;
    }

    @Override
    public void outsider_survival$setItemUseAnimation(OutsiderItemUseAnimation animation, float ticks) {
        outsider_survival$itemUseAnimation = animation;
        outsider_survival$itemUseTime = ticks;
    }

    @Unique
    private Map<@NotNull OutsiderPlayerAnimation, @NotNull Float> outsider_survival$playerAnimationStrengths = Map.of();

    @Override
    public @NotNull Map<@NotNull OutsiderPlayerAnimation, @NotNull Float> outsider_survival$getPlayerAnimations() {
        return outsider_survival$playerAnimationStrengths;
    }

    @Override
    public void outsider_survival$setPlayerAnimations(@NonNull Map<@NotNull OutsiderPlayerAnimation, @NotNull Float> animations) {
        outsider_survival$playerAnimationStrengths = animations;
    }
}
