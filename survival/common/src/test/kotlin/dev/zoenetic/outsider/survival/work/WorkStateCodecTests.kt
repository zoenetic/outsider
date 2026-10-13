package dev.zoenetic.outsider.survival.work

import dev.zoenetic.outsider.survival.fire.LightingFireState
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

    @Test
    fun `lighting a fire survives a round trip`() {
        val state = LightingFireState(BlockPos(12, -40, 7), InteractionHand.OFF_HAND, 35, 160)
        assertEquals(state, roundTrip(state))
    }

    @Test
    fun `every kind decodes to its own type`() {
        val examples: Map<WorkState.Kind, WorkState> = mapOf(
            WorkState.Kind.LIGHTING_FIRE to
                LightingFireState(BlockPos.ZERO, InteractionHand.MAIN_HAND, 0, 1),
        )
        assertEquals(WorkState.Kind.entries.toSet(), examples.keys, "a kind has no example here")
        for ((kind, state) in examples) {
            assertEquals(kind, state.kind, "${state::class.simpleName} reports the wrong kind")
            assertEquals(state, roundTrip(state))
        }
    }
}
