package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.units.Duration
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.NursingEmbers
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.Drilling
import dev.zoenetic.outsider.survival.work.tools.drill.handdrill.LightingFireState.Phase
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
        Phase.Kind.DRILLING to Drilling(
            InteractionHand.OFF_HAND,
            Duration(35),
            Duration(160),
            Heat(212.5),
            true,
            Time(9_000),
        ),
        Phase.Kind.NURSING_EMBERS to NursingEmbers(
            Time(123_456),
            Duration(7),
            Duration(20),
            true,
            Time(123_440),
        ),
    )

    @Test
    fun `lighting a fire with a drill survives a round trip in every phase`() {
        assertEquals(Phase.Kind.entries.toSet(), drillingPhases.keys, "a phase has no example here")
        for ((kind, phase) in drillingPhases) {
            assertEquals(kind, phase.kind, "${phase::class.simpleName} reports the wrong kind")
            val state =
                LightingFireState(BlockPos(12, -40, 7), phase)
            assertEquals(state, roundTrip(state))
        }
    }

    @Test
    fun `every kind decodes to its own type`() {
        val examples: Map<WorkState.Kind, WorkState> = mapOf(
            WorkState.Kind.LIGHTING_FIRE_WITH_DRILL to LightingFireState(
                BlockPos.ZERO,
                Drilling(
                    InteractionHand.MAIN_HAND,
                    Duration.NONE,
                    Duration(1),
                    Heat(20.0),
                    false,
                    Time(0),
                ),
            ),
        )
        assertEquals(WorkState.Kind.entries.toSet(), examples.keys, "a kind has no example here")
        for ((kind, state) in examples) {
            assertEquals(kind, state.kind, "${state::class.simpleName} reports the wrong kind")
            assertEquals(state, roundTrip(state))
        }
    }
}
