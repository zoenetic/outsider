package dev.zoenetic.outsider.survival.campfire

import dev.zoenetic.outsider.survival.fuel.Fuel
import kotlin.math.roundToInt

public enum class CampfireStage(public val id: String) {
    FULL("full"),
    TOP_SUNK("top_sunk"),
    SUNK("sunk"),
}

public object CampfireBurn {
    public const val FRESH_STEP: Int = 15
    public const val SUNK_AT_STEP: Int = 3

    private const val TOP_FRESH_AT = 15
    private const val TOP_BLACK_AT = 5
    private const val BOTTOM_FRESH_AT = 12
    private const val BOTTOM_BLACK_AT = 1

    public fun topStep(fuel: Fuel): Int = step(fuel.level, TOP_FRESH_AT, TOP_BLACK_AT)

    public fun bottomStep(fuel: Fuel): Int = step(fuel.level, BOTTOM_FRESH_AT, BOTTOM_BLACK_AT)

    public fun stage(fuel: Fuel): CampfireStage = when {
        bottomStep(fuel) <= SUNK_AT_STEP -> CampfireStage.SUNK
        topStep(fuel) <= SUNK_AT_STEP -> CampfireStage.TOP_SUNK
        else -> CampfireStage.FULL
    }

    private fun step(level: Int, freshAt: Int, blackAt: Int): Int =
        (FRESH_STEP * (level - blackAt).toDouble() / (freshAt - blackAt))
            .roundToInt()
            .coerceIn(0, FRESH_STEP)
}
