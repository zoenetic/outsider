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
    fun `under an open sky a fire leaves the air alone`() {
        assertEquals(0.0, trapped(6.0, open), 1e-9)
    }

    @Test
    fun `with no sky at all a fire warms the air by the trapping factor`() {
        assertEquals(6.0 * HEAT_TRAPPING, trapped(6.0, sealed), 1e-9)
    }

    @Test
    fun `a half-open sky traps half`() {
        assertEquals(trapped(6.0, sealed) / 2.0, trapped(6.0, Sky(0.5)), 1e-9)
    }

    @Test
    fun `no fire is no fire, however enclosed`() {
        assertEquals(0.0, trapped(0.0, sealed), 1e-9)
    }

    @Test
    fun `trapping never lifts the air past the ceiling`() {
        val air = Heat(20.0)
        assertEquals(MAX_HEATED_AIR.celsius, air.celsius + trapped(6.0, sealed, air), 1e-9)
    }

    @Test
    fun `below the ceiling trapping is not limited`() {
        assertEquals(6.0 * HEAT_TRAPPING, trapped(6.0, sealed, Heat(-20.0)), 1e-9)
    }

    @Test
    fun `air already past the ceiling is not warmed further`() {
        assertEquals(0.0, trapped(32.0, sealed, Heat(40.0)), 1e-9)
    }

    // A cold column, so ambient plus trapped heat stays under the ceiling.
    private val fireAt = seaLevelCentreOf(coldestSite.pos)
    private val standingAt = fireAt.east(2)

    private fun withAndWithoutFire(skyBrightness: Int): Pair<PlayerConditions, PlayerConditions> {
        val world = CommonFixtures.fakeWorld(skyBrightness = skyBrightness)
        val player = world.playerAt(standingAt)
        val time = Time(world.level.gameTime)

        val before = PlayerConditions.getNew(player, time)

        val chunk = world.level.getChunk(
            SectionPos.blockToSectionCoord(fireAt.x),
            SectionPos.blockToSectionCoord(fireAt.z),
        )
        chunk.getSection(chunk.getSectionIndex(fireAt.y))
            .setBlockState(
                fireAt.x and 15,
                fireAt.y and 15,
                fireAt.z and 15,
                Blocks.LAVA.defaultBlockState(),
            )
        val _ = chunk.reconcileEmitters()

        return before to PlayerConditions.getNew(player, time)
    }

    @Test
    fun `the same fire warms the air under a roof but not in the open`() {
        val (openBefore, openAfter) = withAndWithoutFire(skyBrightness = 15)
        val (roofedBefore, roofedAfter) = withAndWithoutFire(skyBrightness = 0)
        assertEquals(openBefore.ambient.celsius, openAfter.ambient.celsius, 1e-9)
        val warmed = roofedAfter.ambient.celsius - roofedBefore.ambient.celsius
        assertEquals(roofedAfter.radiant.celsius * HEAT_TRAPPING, warmed, 1e-6)
    }

    @Test
    fun `a fire warming the air adds no moisture to it`() {
        val (before, after) = withAndWithoutFire(skyBrightness = 0)
        assertTrue(after.ambient > before.ambient, "the roofed fire should warm the air")
        assertEquals(before.vapourPressure, after.vapourPressure, 1e-9)
    }

    @Test
    fun `the fire radiates the same whether roofed or not`() {
        val (_, open) = withAndWithoutFire(skyBrightness = 15)
        val (_, roofed) = withAndWithoutFire(skyBrightness = 0)
        assertTrue(open.radiant.celsius > 0.0, "a fire should radiate at all, got ${open.radiant}")
        assertEquals(open.radiant.celsius, roofed.radiant.celsius, 1e-9)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() = CommonFixtures.bootstrap()
    }
}
