package dev.zoenetic.outsider.survival.stone.loose

import kotlin.math.roundToInt

public object LooseStoneArrangements {

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
    }

    private const val BLOCK_PIXELS: Int = 16

    private val SHAPE_A: Box = Box(4, 10, 6, 12, 1)
    private val SHAPE_B: Box = Box(3, 3, 6, 7, 2)
    private val SHAPE_C: Box = Box(11, 11, 14, 13, 2)
    private val SHAPE_D: Box = Box(9, 5, 11, 8, 1)

    private val SHAPES: List<Box> = listOf(SHAPE_A, SHAPE_B, SHAPE_C, SHAPE_D)

    public val MAX_STONES: Int = SHAPES.size

    private val ARRANGEMENTS_BY_COUNT = (1..SHAPES.size).map { k ->
        enumerate(k)
    }

    private fun enumerate(k: Int): List<List<Box>> {
        val orders = slotOrders(k)
        return shapeSets(k).flatMap { shapes ->
            orders.map { order ->
                shapes.zip(order) { shape, slot -> place(shape, SHAPES[slot]) }
            }
        }
    }

    public fun forCount(stones: Int): List<List<Box>> {
        require(stones in 1..SHAPES.size) {
            "Stones should be between 1 and ${SHAPES.size}, got: $stones"
        }
        return ARRANGEMENTS_BY_COUNT[stones - 1]
    }

    private fun shapeSets(k: Int) = (1..<(1 shl SHAPES.size)).filter { it.countOneBits() == k }
        .map { mask -> SHAPES.filterIndexed { i, _ -> mask and (1 shl i) != 0 } }

    private fun slotOrders(k: Int, picked: List<Int> = emptyList()): List<List<Int>> =
        if (picked.size == k) {
            listOf(picked)
        } else {
            SHAPES.indices.filter { it !in picked }.flatMap { slotOrders(k, picked + it) }
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
}
