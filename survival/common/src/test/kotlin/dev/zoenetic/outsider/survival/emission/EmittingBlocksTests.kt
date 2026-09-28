package dev.zoenetic.outsider.survival.emission

import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.emission.EmitterIndex.isLit
import dev.zoenetic.outsider.survival.emission.EmittingBlock.Companion.emitterOrNull
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmittingBlocksTests {

    private val furnace = Blocks.FURNACE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()

    @Test
    fun `lava counts as a lit heat source`() {
        assertTrue(Blocks.LAVA.defaultBlockState().block.emitterOrNull() != null)
        assertTrue(Blocks.LAVA.defaultBlockState().isLit())
    }

    @Test
    fun `an emitter with a lit property can be unlit`() {
        assertTrue(furnace.block.emitterOrNull() != null)
        assertTrue(furnace.setValue(BlockStateProperties.LIT, true).isLit())
        assertFalse(furnace.setValue(BlockStateProperties.LIT, false).isLit())
    }

    @Test
    fun `a non heat source block is not a heat source`() {
        assertFalse(stone.block.emitterOrNull() != null)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
