package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.fire.LightingFireState
import dev.zoenetic.outsider.survival.fire.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.fire.LightingFireState.Phase
import dev.zoenetic.outsider.survival.registry.OutsiderWork
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import net.minecraft.core.BlockPos
import net.minecraft.core.RegistryAccess
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.Identifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class WorkStateCodecTests {

    private fun buffer() = RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY)

    private fun roundTrip(state: WorkState): WorkState {
        val buffer = buffer()
        WorkState.STREAM_CODEC.encode(buffer, state)
        val decoded = WorkState.STREAM_CODEC.decode(buffer)
        assertEquals(0, buffer.readableBytes(), "bytes left over after decoding $state")
        return decoded
    }

    private val lightingFirePhases: Map<Phase.Kind, Phase> = mapOf(
        Phase.Kind.DRILLING to Drilling(
            dust = Duration(35),
            dustRequired = Duration(160),
            heat = Heat(212.5),
            held = true,
            timestamp = Time(9_000),
        ),
        Phase.Kind.NURSING_EMBERS to NursingEmbers(
            diesAt = Time(123_456),
            blown = Duration(7),
            blownRequired = Duration(20),
            held = true,
            timestamp = Time(123_440),
        ),
    )

    @Test
    fun `lighting a fire survives a round trip in every phase`() {
        assertEquals(Phase.Kind.entries.toSet(), lightingFirePhases.keys, "a phase has no example here")
        for ((kind, phase) in lightingFirePhases) {
            assertEquals(kind, phase.kind, "${phase::class.simpleName} reports the wrong kind")
            val state = LightingFireState(BlockPos(12, -40, 7), phase)
            val decoded = roundTrip(state)
            assertEquals(state, decoded)
            assertSame(state.work, decoded.work)
        }
    }

    @Test
    fun `a registered work is one instance`() {
        assertSame(OutsiderWork.LIGHTING_FIRE, OutsiderWork.LIGHTING_FIRE)
    }

    @Test
    fun `a work decodes to the registered instance`() {
        val buffer = buffer()
        OutsiderWork.STREAM_CODEC.encode(buffer, OutsiderWork.LIGHTING_FIRE)
        assertSame(OutsiderWork.LIGHTING_FIRE, OutsiderWork.STREAM_CODEC.decode(buffer))
    }

    @Test
    fun `an unknown work fails to decode`() {
        val buffer = buffer()
        Identifier.STREAM_CODEC.encode(
            buffer,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "no_such_work"),
        )
        assertFailsWith<DecoderException> { OutsiderWork.STREAM_CODEC.decode(buffer) }
    }
}
