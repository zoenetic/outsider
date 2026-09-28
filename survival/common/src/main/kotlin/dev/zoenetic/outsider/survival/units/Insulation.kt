package dev.zoenetic.outsider.survival.units

@JvmInline
public value class Insulation(public val value: Double) {
    public operator fun times(o: Insulation): Insulation = Insulation(value * o.value)

    public companion object {
        public val NONE: Insulation = Insulation(1.0)
    }
}
