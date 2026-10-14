package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState
import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState.BlowingEmbers
import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState.Drilling
import dev.zoenetic.outsider.survival.fire.LightingFireWithDrillState.Phase
import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Time
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.core.RegistryAccess
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.InteractionHand
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkStateCodecTests {

    private fun roundTrip(state: WorkState): WorkState {
        val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY)
        WorkState.STREAM_CODEC.encode(buffer, state)
        val decoded = WorkState.STREAM_CODEC.decode(buffer)
        assertEquals(0, buffer.readableBytes(), "bytes left over after decoding $state")
        return decoded
    }

    private val drillingPhases: Map<Phase.Kind, Phase> = mapOf(
        Phase.Kind.DRILLING to Drilling(Duration(35), Duration(160)),
        Phase.Kind.BLOWING_EMBERS to BlowingEmbers(Time(123_456), Duration(20)),
    )

    @Test
    fun `lighting a fire with a drill survives a round trip in every phase`() {
        assertEquals(Phase.Kind.entries.toSet(), drillingPhases.keys, "a phase has no example here")
        for ((kind, phase) in drillingPhases) {
            assertEquals(kind, phase.kind, "${phase::class.simpleName} reports the wrong kind")
            val state =
                LightingFireWithDrillState(BlockPos(12, -40, 7), InteractionHand.OFF_HAND, phase)
            assertEquals(state, roundTrip(state))
        }
    }

    @Test
    fun `a new drilling state starts at no progress`() {
        val state =
            LightingFireWithDrillState.new(BlockPos.ZERO, InteractionHand.MAIN_HAND, Duration(160))
        assertEquals(Drilling(Duration.NONE, Duration(160)), state.phase)
        assertEquals(state, roundTrip(state))
    }

    @Test
    fun `every kind decodes to its own type`() {
        val examples: Map<WorkState.Kind, WorkState> = mapOf(
            WorkState.Kind.LIGHTING_FIRE_WITH_DRILL to LightingFireWithDrillState.new(
                BlockPos.ZERO,
                InteractionHand.MAIN_HAND,
                Duration(1),
            ),
        )
        assertEquals(WorkState.Kind.entries.toSet(), examples.keys, "a kind has no example here")
        for ((kind, state) in examples) {
            assertEquals(kind, state.kind, "${state::class.simpleName} reports the wrong kind")
            assertEquals(state, roundTrip(state))
        }
    }
}
