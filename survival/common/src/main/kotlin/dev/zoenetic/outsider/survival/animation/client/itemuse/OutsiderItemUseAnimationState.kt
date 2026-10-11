package dev.zoenetic.outsider.survival.animation.client.itemuse

@Suppress("ktlint:standard:function-naming")
public interface OutsiderItemUseAnimationState {
    public fun `outsider_survival$getItemUseAnimation`(): OutsiderItemUseAnimation?
    public fun `outsider_survival$getItemUseTicks`(): Float
    public fun `outsider_survival$setItemUseAnimation`(
        animation: OutsiderItemUseAnimation?,
        ticks: Float,
    )
}
