package dev.zoenetic.outsider.survival.fire

import dev.zoenetic.outsider.survival.CommonFixtures
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class HearthBoardTests {

    init {
        CommonFixtures.bootstrap()
    }

    private val feet = Vec3(10.5, 64.0, -3.25)

    // The spindle's model-space depth, in blocks, at the 15/16 player scale.
    private val forward = HearthBoard.SPINDLE_PIXELS / 16.0 * 0.9375

    private fun player(bodyYaw: Float): Player = mock(Player::class.java).also {
        doReturn(feet).`when`(it).getPosition(1F)
        doReturn(bodyYaw).`when`(it).getPreciseBodyRotation(1F)
    }

    private fun assertNear(expected: Vec3, actual: Vec3) =
        assertEquals(0.0, expected.distanceTo(actual), 1e-9, "expected $expected, got $actual")

    @Test
    fun `the board lies ahead of the feet along the body's facing`() {
        // Minecraft yaw: 0 faces south (+z), 90 faces west (-x), 180 north, -90 east.
        assertNear(feet.add(0.0, 0.0, forward), HearthBoard.position(player(0F)))
        assertNear(feet.add(-forward, 0.0, 0.0), HearthBoard.position(player(90F)))
        assertNear(feet.add(0.0, 0.0, -forward), HearthBoard.position(player(180F)))
        assertNear(feet.add(forward, 0.0, 0.0), HearthBoard.position(player(-90F)))
    }

    @Test
    fun `the board lies on the ground the player stands on`() {
        for (yaw in listOf(0F, 37F, 135F, -120F)) {
            assertEquals(feet.y, HearthBoard.position(player(yaw)).y)
            assertEquals(forward, HearthBoard.position(player(yaw)).subtract(feet).length(), 1e-9)
        }
    }
}
