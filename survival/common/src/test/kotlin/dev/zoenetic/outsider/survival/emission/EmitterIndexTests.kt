package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.emission.EmitterIndex.reconcileEmitters
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*

class EmitterIndexTests {

    private val inChunk = BlockPos(260, 70, 520)
    private val alsoInChunk = BlockPos(265, 70, 525)
    private val topSection = BlockPos(260, 310, 520)
    private val belowZero = BlockPos(260, -60, 520)

    private val lava = Blocks.LAVA.defaultBlockState()
    private val furnace = Blocks.FURNACE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()
    private val air = Blocks.AIR.defaultBlockState()

    private fun LevelChunk.place(pos: BlockPos, state: BlockState) {
        getSection(getSectionIndex(pos.y))
            .setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state)
    }

    private fun chunk() = CommonFixtures.chunk(CommonFixtures.fakeLevel())

    private fun indexOf(chunk: LevelChunk): Long2LongOpenHashMap = assertNotNull(
        Survival.platform.emitters.get(chunk),
        "no emitter index registered for chunk ${chunk.pos}",
    )

    @Test
    fun `an empty chunk has no heat sources`() {
        val chunk = chunk()
        assertNull(chunk.reconcileEmitters(), "a non-fuelled emitter contributes no drop deadline")
        assertTrue(indexOf(chunk).isEmpty())
    }

    @Test
    fun `two heat sources in one section is two entries`() {
        val chunk = chunk()
        chunk.place(inChunk, lava)
        chunk.place(alsoInChunk, lava)
        assertNull(chunk.reconcileEmitters(), "a non-fuelled emitter contributes no drop deadline")
        val index = indexOf(chunk)
        assertEquals(2, index.size)
        assertTrue(index.containsKey(inChunk.asLong()))
        assertTrue(index.containsKey(alsoInChunk.asLong()))
    }

    @Test
    fun `a heat source in the top section is found`() {
        val chunk = chunk()
        chunk.place(topSection, lava)
        assertNull(chunk.reconcileEmitters(), "a non-fuelled emitter contributes no drop deadline")
        assertTrue(indexOf(chunk).containsKey(topSection.asLong()))
    }

    @Test
    fun `a heat source at negative y is found`() {
        val chunk = chunk()
        chunk.place(belowZero, lava)
        assertNull(chunk.reconcileEmitters(), "a non-fuelled emitter contributes no drop deadline")
        val key = indexOf(chunk).keys.single()
        assertEquals(belowZero, BlockPos.of(key), "packed key round-trips through BlockPos.of")
    }

    @Test
    fun `a heat source replaced by air is not in the index`() {
        val chunk = chunk()
        chunk.place(alsoInChunk, stone)
        chunk.place(inChunk, lava)
        chunk.place(inChunk, air)
        assertNull(chunk.reconcileEmitters(), "a non-fuelled emitter contributes no drop deadline")
        assertTrue(indexOf(chunk).isEmpty())
    }

    @Test
    fun `putting then removing a single source clears it from the index`() {
        val chunk = chunk()
        EmitterIndex.onBlockChanged(chunk, inChunk, air, lava)
        assertTrue(indexOf(chunk).containsKey(inChunk.asLong()))
        EmitterIndex.remove(chunk, inChunk)
        assertFalse(indexOf(chunk).containsKey(inChunk.asLong()))
    }

    @Test
    fun `removing a non-existent source does not throw and leaves nothing behind`() {
        val chunk = chunk()
        EmitterIndex.remove(chunk, inChunk)
        assertTrue(indexOf(chunk).isEmpty())
    }

    @Test
    fun `changing a block to a non-source no-ops the index`() {
        val chunk = chunk()
        EmitterIndex.onBlockChanged(chunk, inChunk, air, stone)
        assertTrue(indexOf(chunk).isEmpty())
    }

    @Test
    fun `updating one heat source to another leaves one entry`() {
        val chunk = chunk()
        EmitterIndex.onBlockChanged(chunk, inChunk, air, lava)
        EmitterIndex.onBlockChanged(chunk, inChunk, lava, furnace)
        val index = indexOf(chunk)
        assertEquals(1, index.size)
    }

    @Test
    fun `replacing a single source with air drops that blockpos from the index`() {
        val chunk = chunk()
        EmitterIndex.onBlockChanged(chunk, inChunk, air, lava)
        EmitterIndex.onBlockChanged(chunk, inChunk, lava, air)
        assertFalse(indexOf(chunk).containsKey(inChunk.asLong()))
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
