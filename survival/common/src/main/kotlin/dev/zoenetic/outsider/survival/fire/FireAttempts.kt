package dev.zoenetic.outsider.survival.fire

import net.minecraft.core.BlockPos
import java.util.UUID

internal const val LIGHTING_FIRE_GRACE_PERIOD = 10L

public class FireAttempts {

    private data class AttemptKey(val uuid: UUID, val pos: BlockPos) {
        constructor(uuid: UUID, pos: BlockPos, immutable: Boolean = true) : this(
            uuid,
            pos.immutable(),
        )
    }

    public data class Attempts(val count: Int, val time: Long)

    private val attempts = mutableMapOf<AttemptKey, Attempts>()

    public fun getAttempts(uuid: UUID, pos: BlockPos): Attempts? = attempts[AttemptKey(uuid, pos)]

    public fun recordAttempt(uuid: UUID, pos: BlockPos, time: Long): Int {
        val key = AttemptKey(uuid, pos)
        val previous = attempts[key]
        val count =
            if (previous == null ||
                time - previous.time > LIGHTING_FIRE_GRACE_PERIOD
            ) {
                1
            } else {
                previous.count + 1
            }
        attempts[key] = Attempts(count, time)
        return count
    }

    public fun clear(uuid: UUID, pos: BlockPos) {
        attempts.remove(AttemptKey(uuid, pos))
    }

    public fun clear(uuid: UUID) {
        attempts.keys.removeIf { it.uuid == uuid }
    }
}
