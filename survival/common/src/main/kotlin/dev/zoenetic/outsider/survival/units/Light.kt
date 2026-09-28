package dev.zoenetic.outsider.survival.units

public data class Light(public val value: Int) {
    init {
        require(value in 0..15) { "Light value out of range: $value" }
    }

    public operator fun compareTo(o: Light): Int = value.compareTo(o.value)

    public companion object {
        public val ZERO: Light = Light(0)
        public val MAX: Light = Light(15)
    }
}
