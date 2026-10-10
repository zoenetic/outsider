package dev.zoenetic.outsider.survival.fuel

import dev.zoenetic.outsider.survival.units.Time
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.world.level.ChunkPos

/**
 * Get from a chunk key to the earliest time a fuelledblock in the chunk might
 * drop a fuel level. Only includes chunks with something still burning.
 *
 * This can be rebuilt from the emitter index, which is the authority on this data.
 * The stored value is the lower bound, it's never later than the earliest deadline
 */
public class DropSchedule {

    private val byChunk = Long2LongOpenHashMap().apply { defaultReturnValue(Long.MAX_VALUE) }

    /** Bring a chunk's deadline forward if [at] is sooner. */
    public fun notice(chunk: ChunkPos, at: Time) {
        val key = chunk.pack()
        if (at.value < byChunk.get(key)) byChunk.put(key, at.value)
    }

    /** Set a chunk's deadline directly; only valid immediately after it's been recomputed by a scan */
    public fun reset(chunk: ChunkPos, at: Time?) {
        val key = chunk.pack()
        if (at == null) byChunk.remove(key) else byChunk.put(key, at.value)
    }

    public fun due(now: Time): List<ChunkPos> {
        val due = mutableListOf<ChunkPos>()
        val entries = Long2LongMaps.fastIterator(byChunk)
        while (entries.hasNext()) {
            val entry = entries.next()
            if (entry.longValue <= now.value) due.add(ChunkPos.unpack(entry.longKey))
        }
        return due
    }
}
