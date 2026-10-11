package dev.zoenetic.outsider.survival.animation.client.player

import org.jetbrains.annotations.NotNull

@Suppress("ktlint:standard:function-naming")
public interface OutsiderPlayerAnimationState {
    public fun `outsider_survival$getPlayerAnimations`():
        @NotNull Map<@NotNull OutsiderPlayerAnimation, @NotNull Float>

    public fun `outsider_survival$setPlayerAnimations`(
        animations: @NotNull Map<@NotNull OutsiderPlayerAnimation, @NotNull Float>,
    )
}
