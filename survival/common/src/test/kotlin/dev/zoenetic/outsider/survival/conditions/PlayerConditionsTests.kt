package dev.zoenetic.outsider.survival.conditions

import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.CommonFixtures.coldestSite
import dev.zoenetic.outsider.survival.CommonFixtures.hottestSite
import dev.zoenetic.outsider.survival.CommonFixtures.seaLevelCentreOf
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*

class PlayerConditionsTests {

    private fun CommonFixtures.FakeWorld.sample(player: ServerPlayer): PlayerConditions =
        PlayerConditions.getNew(player, Time(level.gameTime))

    private fun CommonFixtures.FakeWorld.sampleAt(pos: BlockPos): PlayerConditions =
        sample(playerAt(pos))

    @Test
    fun `the climate chunk consulted is the one containing the player`() {
        val world = CommonFixtures.fakeWorld()
        val standingOn = BlockPos(1234, CommonFixtures.SEA_LEVEL, -5678)

        val _ = world.sample(world.playerAt(standingOn))

        val expected = ChunkPos(
            SectionPos.blockToSectionCoord(standingOn.x),
            SectionPos.blockToSectionCoord(standingOn.z),
        )

        assertEquals(
            expected,
            world.requestedChunks.first(),
            "conditions were read from ${world.requestedChunks.first()} " +
                "but the player is standing in $expected",
        )
    }

    @Test
    fun `conditions report the climate of the player's own column`() {
        val world = CommonFixtures.fakeWorld()
        val conditions = world.sampleAt(seaLevelCentreOf(hottestSite.pos))
        assertEquals(
            hottestSite.temperature.celsius,
            conditions.ambient.celsius,
            1e-9,
            "player is in ${hottestSite.pos}, which the scan puts at " +
                "${hottestSite.temperature.celsius}°C",
        )
    }

    @Test
    fun `standing somewhere hot reads hot`() {
        val conditions = CommonFixtures.fakeWorld().sampleAt(seaLevelCentreOf(hottestSite.pos))
        assertTrue(
            conditions.ambient.celsius > 25.0,
            "the hottest column in the world read ${conditions.ambient.celsius}°C",
        )
    }

    @Test
    fun `standing somewhere cold reads cold`() {
        val conditions = CommonFixtures.fakeWorld().sampleAt(seaLevelCentreOf(coldestSite.pos))
        assertTrue(
            conditions.ambient.celsius < 5.0,
            "the coldest column in the world read ${conditions.ambient.celsius}°C",
        )
    }

    @Test
    fun `moving between climates changes the reported temperature`() {
        val world = CommonFixtures.fakeWorld()
        val hot = world.sampleAt(seaLevelCentreOf(hottestSite.pos))
        val cold = world.sampleAt(seaLevelCentreOf(coldestSite.pos))
        assertTrue(
            hot.ambient.celsius > cold.ambient.celsius,
            "hot ${hot.ambient.celsius}°C should exceed cold ${cold.ambient.celsius}°C",
        )
    }

    @Test
    fun `altitude cools the reading above sea level`() {
        val world = CommonFixtures.fakeWorld()
        val ground = seaLevelCentreOf(hottestSite.pos)
        val atGround = world.sampleAt(ground)
        val onAPeak = world.sampleAt(ground.above(200))
        assertTrue(
            onAPeak.ambient.celsius < atGround.ambient.celsius,
            "200 blocks up read ${onAPeak.ambient.celsius}°C " +
                "against ${atGround.ambient.celsius}°C at sea level",
        )
    }

    @Test
    fun `an open sky admits a diurnal swing that a sealed one does not`() {
        val pos = seaLevelCentreOf(hottestSite.pos)
        val sealed = CommonFixtures.fakeWorld(skyBrightness = 0, clockTime = 9000L)
        val open = CommonFixtures.fakeWorld(skyBrightness = 15, clockTime = 9000L)
        assertNotEquals(
            sealed.sampleAt(pos).ambient.celsius,
            open.sampleAt(pos).ambient.celsius,
            "sky brightness should feed the diurnal swing",
        )
    }

    @Test
    fun `the diurnal swing follows the day clock, not total world age`() {
        val pos = seaLevelCentreOf(hottestSite.pos)
        val warmest = CommonFixtures.fakeWorld(
            skyBrightness = 15,
            gameTime = 500_000L,
            clockTime = 9000L,
        )
        val coldest = CommonFixtures.fakeWorld(
            skyBrightness = 15,
            gameTime = 500_000L,
            clockTime = 21000L,
        )
        assertTrue(
            warmest.sampleAt(pos).ambient.celsius > coldest.sampleAt(pos).ambient.celsius,
            "mid-afternoon should beat pre-dawn even at the same game time",
        )
    }

    @Test
    fun `conditions record the monotonic game time, not the day clock`() {
        val world = CommonFixtures.fakeWorld(gameTime = 500_000L, clockTime = 9000L)
        val conditions = world.sampleAt(seaLevelCentreOf(hottestSite.pos))
        assertEquals(500_000L, conditions.time.value, "elapsed needs a clock that never rewinds")
    }

    @Test
    fun `a first tick stores conditions the store can read back`() {
        val world = CommonFixtures.fakeWorld(gameTime = 500_000L)
        val player = world.playerAt(seaLevelCentreOf(hottestSite.pos))

        PlayerConditions.tick(player, Time(world.level.gameTime))

        val stored = assertNotNull(
            PlayerConditions.get(player),
            "a player's first tick should leave conditions in the store, not null",
        )
        assertEquals(500_000L, stored.time.value, "stored conditions should be stamped now")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
