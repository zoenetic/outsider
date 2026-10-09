package dev.zoenetic.outsider.survival.stone.loose

import com.mojang.serialization.JsonOps
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangement.Box
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LooseStoneArrangementTests {
    // Every type registered in OutsiderLooseStoneBlocks. Loose obsidian is deliberately absent.
    private val types = mapOf(
        "andesite" to LooseStoneArrangements.ANDESITE,
        "basalt" to LooseStoneArrangements.BASALT,
        "blackstone" to LooseStoneArrangements.BLACKSTONE,
        "calcite" to LooseStoneArrangements.CALCITE,
        "deepslate" to LooseStoneArrangements.DEEPSLATE,
        "diorite" to LooseStoneArrangements.DIORITE,
        "end stone" to LooseStoneArrangements.ENDSTONE,
        "granite" to LooseStoneArrangements.GRANITE,
        "red sandstone" to LooseStoneArrangements.RED_SANDSTONE,
        "sandstone" to LooseStoneArrangements.SANDSTONE,
        "stone" to LooseStoneArrangements.STONE,
        "tuff" to LooseStoneArrangements.TUFF,
    )

    private val stone = LooseStoneArrangements.STONE

    // Stone's tables, pinned: its models are already in worlds and datagen.
    private val home = listOf(
        Box(4, 10, 6, 12, 1),
        Box(3, 3, 6, 7, 2),
        Box(11, 11, 14, 13, 2),
        Box(9, 5, 11, 8, 1),
    )

    // Row = shape A..D, column = slot (the home of shape A..D).
    private val grid = listOf(
        listOf(
            Box(4, 10, 6, 12, 1),
            Box(4, 4, 6, 6, 1),
            Box(12, 11, 14, 13, 1),
            Box(9, 6, 11, 8, 1),
        ),
        listOf(
            Box(4, 9, 7, 13, 2),
            Box(3, 3, 6, 7, 2),
            Box(11, 10, 14, 14, 2),
            Box(9, 5, 12, 9, 2),
        ),
        listOf(
            Box(4, 10, 7, 12, 2),
            Box(3, 4, 6, 6, 2),
            Box(11, 11, 14, 13, 2),
            Box(9, 6, 12, 8, 2),
        ),
        listOf(
            Box(4, 10, 6, 13, 1),
            Box(4, 4, 6, 7, 1),
            Box(12, 11, 14, 14, 1),
            Box(9, 5, 11, 8, 1),
        ),
    )

    private fun overlaps(a: Box, b: Box): Boolean =
        a.minX < b.maxX && b.minX < a.maxX && a.minZ < b.maxZ && b.minZ < a.maxZ

    private fun gap(a: Box, b: Box): Int =
        maxOf(b.minX - a.maxX, a.minX - b.maxX, b.minZ - a.maxZ, a.minZ - b.maxZ)

    @Test
    fun `every count has one arrangement per layout`() {
        for ((name, type) in types) {
            assertEquals(listOf(576, 576, 576, 576), (1..4).map { type.forCount(it).size }, name)
        }
    }

    @Test
    fun `each count has every placement of that many shapes`() {
        for ((name, type) in types) {
            val distinct = (1..4).map { type.forCount(it).distinct().size }
            assertEquals(listOf(16, 72, 96, 24), distinct, "$name: are two shapes the same size?")
        }
    }

    @Test
    fun `every distinct arrangement is equally likely`() {
        for ((name, type) in types) {
            for (count in 1..4) {
                val frequencies = type.forCount(count).groupingBy { it }.eachCount().values.toSet()
                assertEquals(1, frequencies.size, "$name, count $count: $frequencies")
            }
        }
    }

    @Test
    fun `every arrangement has as many stones as its count`() {
        for ((name, type) in types) {
            for (count in 1..4) {
                assertTrue(type.forCount(count).all { it.size == count }, "$name, count $count")
            }
        }
    }

    // The fix for clusters reshuffling: a position keeps its layout index at every count.
    @Test
    fun `adding a stone keeps the others in place`() {
        for ((name, type) in types) {
            for (count in 1..3) {
                val fewer = type.forCount(count)
                val more = type.forCount(count + 1)
                for (i in fewer.indices) {
                    assertTrue(more[i].containsAll(fewer[i]), "$name, layout $i, count $count")
                }
            }
        }
    }

    @Test
    fun `stones in one arrangement never overlap`() {
        for ((name, type) in types) {
            for (arrangement in type.forCount(4).distinct()) {
                val stones = arrangement.toList()
                for (i in stones.indices) {
                    for (j in i + 1 until stones.size) {
                        val (a, b) = stones[i] to stones[j]
                        assertTrue(!overlaps(a, b), "$name: $a and $b overlap")
                    }
                }
            }
        }
    }

    // A design rule rather than a hard limit: touching stones read as one lump.
    @Test
    fun `stones keep at least two pixels apart`() {
        for ((name, type) in types) {
            for (arrangement in type.forCount(4).distinct()) {
                val stones = arrangement.toList()
                for (i in stones.indices) {
                    for (j in i + 1 until stones.size) {
                        val (a, b) = stones[i] to stones[j]
                        assertTrue(gap(a, b) >= 2, "$name: $a and $b are ${gap(a, b)} px apart")
                    }
                }
            }
        }
    }

    // Catches a type's table pasted over another's.
    @Test
    fun `every type has its own arrangements`() {
        val byLayouts = types.entries.groupBy({ it.value.forCount(4).toSet() }, { it.key })
        val shared = byLayouts.values.filter { it.size > 1 }
        assertTrue(shared.isEmpty(), "types sharing one table: $shared")
    }

    @Test
    fun `an arrangement survives its codec`() {
        for ((name, type) in types) {
            val codec = LooseStoneArrangement.CODEC.codec()
            val json = codec.encodeStart(JsonOps.INSTANCE, type).orThrow
            val decoded = codec.parse(JsonOps.INSTANCE, json).orThrow
            assertEquals(type.forCount(4), decoded.forCount(4), name)
        }
    }

    @Test
    fun `a single stone covers every shape in every slot, shape by shape`() {
        val singles = stone.forCount(1).distinct().map { it.single() }
        assertEquals(grid.flatten(), singles)
    }

    @Test
    fun `the first full arrangement is the hand-made layout`() {
        assertEquals(home, stone.forCount(4).first().toList())
        for ((name, type) in types) {
            val boxes = listOf(type.a, type.b, type.c, type.d)
            assertEquals(boxes, type.forCount(4).first().toList(), name)
        }
    }

    // Pinned so any change to the layout order fails here: the variant list order in the
    // blockstate and the outline lookup both depend on it.
    @Test
    fun `the layout order is stable`() {
        fun at(shape: Int, slot: Int) = grid[shape][slot]
        assertEquals(setOf(at(0, 3), at(1, 2), at(2, 1), at(3, 0)), stone.forCount(4)[23])
        assertEquals(setOf(at(3, 3), at(2, 2)), stone.forCount(2)[575])
        assertEquals(setOf(at(0, 1), at(1, 0), at(3, 2)), stone.forCount(3)[30])
    }

    @Test
    fun `counts outside one to four are refused`() {
        assertFailsWith<IllegalArgumentException> { stone.forCount(0) }
        assertFailsWith<IllegalArgumentException> { stone.forCount(5) }
    }

    @Test
    fun `a box at the block edge is allowed`() {
        val _ = Box(0, 0, 16, 16, 1)
    }
}
