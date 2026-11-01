package dev.zoenetic.outsider.survival.fire.client

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import dev.zoenetic.outsider.survival.CommonFixtures
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.fire.LightingFireState.Phase
import dev.zoenetic.outsider.survival.fire.LightingFireState.Phase.Kind
import dev.zoenetic.outsider.survival.fire.emberProgress
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InLightingPhaseTests {

    init {
        CommonFixtures.bootstrap()
    }

    private val drilling = Drilling(
        dust = Duration(35),
        dustRequired = Duration(160),
        heat = Heat(212.5),
        held = false,
        timestamp = Time(9_000),
    )

    private val nursing = NursingEmbers(
        heat = Heat(450.0),
        diesAt = Time(123_456),
        blown = Duration(7),
        blownRequired = Duration(20),
        breathUntil = Time(9_060),
        nextBreathAt = Time(8_990),
        held = true,
        timestamp = Time(123_440),
    )

    private fun player(phase: Phase?): Player = mock(Player::class.java).also { player ->
        if (phase != null) {
            Survival.platform.workState.set(player, LightingFireState(Vec3.ZERO, phase))
        }
    }

    private fun inPhase(
        kind: Kind,
        player: Player?,
        context: ItemDisplayContext = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
    ): Boolean = InLightingPhase(kind).get(ItemStack.EMPTY, null, player, 0, context)

    private fun isDrilling(
        player: Player?,
        context: ItemDisplayContext = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
    ): Boolean = inPhase(Kind.DRILLING, player, context)

    @Test
    fun `drilling shows in either hand and either view, resting or not`() {
        val player = player(drilling)
        for (context in listOf(
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
        )) {
            assertTrue(isDrilling(player, context), "$context")
        }
    }

    @Test
    fun `drilling never shows outside the hand`() {
        val player = player(drilling)
        for (context in listOf(
            ItemDisplayContext.GUI,
            ItemDisplayContext.GROUND,
            ItemDisplayContext.FIXED,
            ItemDisplayContext.HEAD,
        )) {
            assertFalse(isDrilling(player, context), "$context")
        }
    }

    @Test
    fun `nursing the ember is not drilling`() {
        assertFalse(isDrilling(player(nursing)))
    }

    @Test
    fun `no work or no holder is not drilling`() {
        assertFalse(isDrilling(player(null)))
        assertFalse(isDrilling(null))
    }

    @Test
    fun `nursing the embers is its own phase`() {
        assertTrue(inPhase(Kind.NURSING_EMBERS, player(nursing)))
        assertFalse(inPhase(Kind.NURSING_EMBERS, player(drilling)))
        assertFalse(inPhase(Kind.NURSING_EMBERS, player(nursing), ItemDisplayContext.GUI))
    }

    @Test
    fun `the phase field reads and writes lowercase phase names`() {
        for (kind in Kind.entries) {
            val json = InLightingPhase.MAP_CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, InLightingPhase(kind)).getOrThrow()
            assertEquals(kind.name.lowercase(), json.asJsonObject["phase"].asString)
            val decoded = InLightingPhase.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow()
            assertEquals(InLightingPhase(kind), decoded)
        }
    }

    @Test
    fun `an unknown phase name fails to load`() {
        val json = JsonParser.parseString("""{"phase": "smouldering"}""")
        assertTrue(InLightingPhase.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).isError)
    }

    @Test
    fun `ember heat reads the nursing ember's progress`() {
        val player = player(nursing)
        doReturn(player).`when`(player).asLivingEntity()
        assertEquals(emberProgress(nursing), EmberHeat.get(ItemStack.EMPTY, null, player, 0))
    }

    @Test
    fun `ember heat is nothing outside nursing the embers`() {
        val drillingPlayer = player(drilling)
        doReturn(drillingPlayer).`when`(drillingPlayer).asLivingEntity()
        assertEquals(0F, EmberHeat.get(ItemStack.EMPTY, null, drillingPlayer, 0))
        assertEquals(0F, EmberHeat.get(ItemStack.EMPTY, null, null, 0))
    }
}
