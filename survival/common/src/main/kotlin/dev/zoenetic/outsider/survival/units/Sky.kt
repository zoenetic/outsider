package dev.zoenetic.outsider.survival.units

@JvmInline
public value class Sky(public val value: Double) {
    public companion object {
        public fun fromBrightness(brightness: Int, max: Int): Sky =
            Sky(brightness.toDouble() / max)
    }
}