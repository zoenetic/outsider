package dev.zoenetic.outsider.survival.stone.loose

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlin.math.roundToInt

private const val BLOCK_PIXELS: Int = 16
public const val MAX_STONES: Int = 4

public class LooseStoneArrangement(
    public val a: Box,
    public val b: Box,
    public val c: Box,
    public val d: Box,
) {

    public data class Box(
        public val minX: Int,
        public val minZ: Int,
        public val maxX: Int,
        public val maxZ: Int,
        public val height: Int,
    ) {
        init {
            require(minX >= 0) { "minX must be >= 0, got: $minX" }
            require(minZ >= 0) { "minZ must be >= 0, got: $minZ" }
            require(minX < maxX) { "minX must be < maxX, got: $minX" }
            require(minZ < maxZ) { "minZ must be < maxZ, got: $minZ" }
            require(maxX <= BLOCK_PIXELS) { "maxX must be <= $BLOCK_PIXELS, got: $maxX" }
            require(maxZ <= BLOCK_PIXELS) { "maxZ must be <= $BLOCK_PIXELS, got: $maxZ" }
            require(height >= 1) { "height must be >= 1, got: $height" }
            require(height <= BLOCK_PIXELS) { "height must be <= $BLOCK_PIXELS, got: $height" }
        }

        public companion object {
            public val CODEC: Codec<Box> =
                RecordCodecBuilder.create { i: RecordCodecBuilder.Instance<Box> ->
                    i.group(
                        Codec.INT.fieldOf("min_x").forGetter(Box::minX),
                        Codec.INT.fieldOf("min_z").forGetter(Box::minZ),
                        Codec.INT.fieldOf("max_x").forGetter(Box::maxX),
                        Codec.INT.fieldOf("max_z").forGetter(Box::maxZ),
                        Codec.INT.fieldOf("height").forGetter(Box::height),
                    ).apply(i, ::Box)
                }
        }
    }

    private val shapes: List<Box> = listOf(a, b, c, d)

    private val layouts: List<List<Box>> = permutations(shapes.size).flatMap { fillOrder ->
        permutations(shapes.size).map { slots ->
            fillOrder.zip(slots) { shape, slot -> place(shapes[shape], shapes[slot]) }
        }
    }

    private val arrangementsByCount: List<List<Set<Box>>> = (1..MAX_STONES).map { k ->
        layouts.map { it.take(k).toSet() }
    }

    public fun forCount(stones: Int): List<Set<Box>> {
        require(stones in 1..shapes.size) {
            "Stones should be between 1 and ${shapes.size}, got: $stones"
        }
        return arrangementsByCount[stones - 1]
    }

    private fun permutations(n: Int, picked: List<Int> = emptyList()): List<List<Int>> =
        if (picked.size == n) {
            listOf(picked)
        } else {
            (0..<n).filter { it !in picked }.flatMap { permutations(n, picked + it) }
        }

    private fun place(shape: Box, slot: Box): Box {
        val width = shape.maxX - shape.minX
        val depth = shape.maxZ - shape.minZ
        val centreX = (slot.minX + slot.maxX).toDouble() / 2.0
        val centreZ = (slot.minZ + slot.maxZ).toDouble() / 2.0
        val minX = (centreX - (width / 2.0)).roundToInt()
        val minZ = (centreZ - (depth / 2.0)).roundToInt()
        val maxX = minX + width
        val maxZ = minZ + depth
        return Box(minX, minZ, maxX, maxZ, shape.height)
    }

    public companion object {
        public val CODEC: MapCodec<LooseStoneArrangement> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<LooseStoneArrangement> ->
                i.group(
                    Box.CODEC.fieldOf("a").forGetter(LooseStoneArrangement::a),
                    Box.CODEC.fieldOf("b").forGetter(LooseStoneArrangement::b),
                    Box.CODEC.fieldOf("c").forGetter(LooseStoneArrangement::c),
                    Box.CODEC.fieldOf("d").forGetter(LooseStoneArrangement::d),
                ).apply(i, ::LooseStoneArrangement)
            }
    }
}
