package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.CommonFixtures.coldestSite
import dev.zoenetic.outsider.survival.CommonFixtures.seaLevelCentreOf
import dev.zoenetic.outsider.survival.emission.EmitterIndex.reconcileEmitters
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Sky
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.SectionPos
import net.minecraft.world.level.block.Blocks
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeatTrappingTests {

    private val open = Sky(1.0)
    private val sealed = Sky(0.0)

    private val cold = Heat(-7.5)

    private fun trapped(heat: Double, sky: Sky, ambient: Heat = cold): Double =
        trapHeat(Heat(heat), sky, ambient).celsius

    @Test
    fun `under an open sky a fire warms exactly as much as it radiates`() {
        assertEquals(6.0, trapped(6.0, open), 1e-9)
    }

    @Test
    fun `with no sky at all the same fire warms by one plus the trapping factor`() {
        assertEquals(6.0 * (1.0 + HEAT_TRAPPING), trapped(6.0, sealed), 1e-9)
    }

    @Test
    fun `a half-open sky traps half the extra`() {
        val extra = trapped(6.0, sealed) - trapped(6.0, open)
        assertEquals(6.0 + extra / 2.0, trapped(6.0, Sky(0.5)), 1e-9)
    }

    @Test
    fun `no fire is no fire, however enclosed`() {
        assertEquals(0.0, trapped(0.0, sealed), 1e-9)
    }

    @Test
    fun `trapping never lifts the air past the ceiling`() {
        val ambient = Heat(20.0)
        val air = ambient.celsius + trapped(6.0, sealed, ambient)
        assertEquals(MAX_HEATED_AIR.celsius, air, 1e-9)
    }

    @Test
    fun `below the ceiling trapping is not limited`() {
        val ambient = Heat(-20.0)
        assertEquals(6.0 * (1.0 + HEAT_TRAPPING), trapped(6.0, sealed, ambient), 1e-9)
    }

    @Test
    fun `radiant heat is never capped, even in air already past the ceiling`() {
        val desert = Heat(40.0)
        assertEquals(32.0, trapped(32.0, sealed, desert), 1e-9)
        assertEquals(32.0, trapped(32.0, open, desert), 1e-9)
    }

    // A cold column, so ambient plus trapped heat stays under the ceiling.
    private val fireAt = seaLevelCentreOf(coldestSite.pos)
    private val standingAt = fireAt.east(2)

    private fun fireContribution(skyBrightness: Int): Double {
        val world = CommonFixtures.fakeWorld(skyBrightness = skyBrightness)
        val player = world.playerAt(standingAt)
        val time = Time(world.level.gameTime)

        val cold = PlayerConditions.getNew(player, time).ambient.celsius

        val chunk = world.level.getChunk(
            SectionPos.blockToSectionCoord(fireAt.x),
            SectionPos.blockToSectionCoord(fireAt.z),
        )
        chunk.getSection(chunk.getSectionIndex(fireAt.y))
            .setBlockState(
                fireAt.x and 15,
                fireAt.y and 15,
                fireAt.z and 15,
                Blocks.LAVA.defaultBlockState()
            )
        val _ = chunk.reconcileEmitters()

        val warm = PlayerConditions.getNew(player, time).ambient.celsius
        return warm - cold
    }

    @Test
    fun `the same fire warms the air more under a roof than in the open`() {
        val inTheOpen = fireContribution(skyBrightness = 15)
        val underARoof = fireContribution(skyBrightness = 0)
        assertTrue(inTheOpen > 0.0, "a fire should warm the air at all, got $inTheOpen")
        assertTrue(
            underARoof > inTheOpen,
            "roofed $underARoof should exceed open $inTheOpen"
        )
        assertEquals(inTheOpen * (1.0 + HEAT_TRAPPING), underARoof, 1e-6)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() = CommonFixtures.bootstrap()
    }
}
