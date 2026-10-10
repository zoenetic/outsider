package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.units.Light

@JvmInline
public value class LightTable(private val packed: Long) {

    public operator fun get(fuel: Int): Int {
        require(fuel in 0..15) { "fuel must be 0..15, got $fuel" }
        return ((packed ushr (fuel * 4)) and 0xF).toInt()
    }

    internal fun with(fuel: Int, light: Light): LightTable {
        require(fuel in 0..15) { "level must be 0..15, got $fuel" }
        require(light.value in 0..15) { "value must be 0..15, got $light.value" }
        val shift = fuel * 4
        val cleared = packed and (0xFL shl shift).inv()
        return LightTable(cleared or (light.value.toLong() shl shift))
    }

    public fun toIntArray(): IntArray = IntArray(16) { this[it] }

    override fun toString(): String =
        toIntArray().joinToString(prefix = "LightTable[", postfix = "]")

    public companion object {
        public val IDENTITY: LightTable = LightTable(0xFEDCBA9876543210uL.toLong())
        public val ZERO: LightTable = LightTable(0L)

        public fun of(vararg values: Int): LightTable {
            require(values.size == 16) { "need 16 values, got ${values.size}" }
            var acc = 0L
            values.forEachIndexed { i, v ->
                require(v in 0..15) { "value must be 0..15, got $v" }
                acc = acc or (v.toLong() shl (i * 4))
            }
            return LightTable(acc)
        }

        public fun from(f: (Int) -> Int): LightTable = of(*IntArray(16, f))
    }
}
