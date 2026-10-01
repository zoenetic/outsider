package dev.zoenetic.outsider.survival.emission

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import dev.zoenetic.outsider.survival.ServerState
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.emission.EmittingBlock.Companion.emitterOrNull
import dev.zoenetic.outsider.survival.fuel.Burnout
import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.fuel.FuelledBlock.Companion.fuelledOrNull
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.network.VarInt
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.stream.LongStream

public object EmitterIndex {

    public const val ABSENT: Long = Long.MIN_VALUE
    internal const val EMISSION_SOFTENING = 1.0

    internal val MAX_RADIUS: Int
        get() = 16

    public fun create(expected: Int = 16): Long2LongOpenHashMap {
        val map = Long2LongOpenHashMap(expected)
        map.defaultReturnValue(ABSENT)
        return map
    }

    internal fun at(level: ServerLevel, pos: BlockPos, radius: Int): List<BlockPos> {
        val minPos = getMinPos(level, pos, radius)
        val maxPos = getMaxPos(level, pos, radius)
        val minChunkX = SectionPos.blockToSectionCoord(minPos.x)
        val minChunkZ = SectionPos.blockToSectionCoord(minPos.z)
        val maxChunkX = SectionPos.blockToSectionCoord(maxPos.x)
        val maxChunkZ = SectionPos.blockToSectionCoord(maxPos.z)
        val emitters = mutableListOf<BlockPos>()
        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                val chunk = level.getChunk(chunkX, chunkZ)
                val chunkEmitters = Survival.platform.emitters.get(chunk) ?: continue
                for ((pos) in chunkEmitters) {
                    val blockPos = BlockPos.of(pos)
                    val state = chunk.getBlockState(blockPos)
                    if (state.block.emitterOrNull() == null) continue
                    emitters.add(blockPos)
                }
            }
        }
        return emitters
    }

    public fun atPlayer(player: ServerPlayer): List<BlockPos> {
        val level = player.level()
        val pos = player.blockPosition()
        return at(level, pos, MAX_RADIUS)
    }

    internal fun getMaxPos(level: ServerLevel, pos: BlockPos, radius: Int): BlockPos {
        val maxY = (pos.y + radius).coerceAtMost(level.maxY)
        return BlockPos(pos.x + radius, maxY, pos.z + radius)
    }

    internal fun getMinPos(level: ServerLevel, pos: BlockPos, radius: Int): BlockPos {
        val minY = (pos.y - radius).coerceAtLeast(level.minY)
        return BlockPos(pos.x - radius, minY, pos.z - radius)
    }

    public fun heatAtPlayer(player: ServerPlayer): Heat {
        val bodyPos = player.boundingBox.center
        val blockPos = player.blockPosition()
        val level = player.level()
        val emitters = at(level, blockPos, MAX_RADIUS)
        var total = Heat(0.0)
        for (pos in emitters) {
            val state = level.getBlockState(pos)
            if (!state.isLit()) continue
            val power = state.heat() ?: continue
            total += heatFrom(bodyPos, pos, power)
        }
        return total
    }

    internal fun heatFrom(body: Vec3, source: BlockPos, heat: Heat): Heat {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(source))
        return Heat(heat.celsius / (distanceSq + EMISSION_SOFTENING))
    }

    public fun BlockState.isLit(): Boolean {
        return getValueOrElse(BlockStateProperties.LIT, true)
    }

    internal fun BlockState.heat(): Heat? {
        val block = block
        val emitter = block.emitterOrNull() ?: return null
        return emitter.getHeat(this)
    }

    @JvmStatic
    public fun lightEmission(state: BlockState): Int {
        val block = state.block
        val emitter = block.emitterOrNull() ?: return 0
        return emitter.getLight(state).value
    }

    @JvmStatic
    public fun onBlockChanged(
        chunk: LevelChunk,
        blockPos: BlockPos,
        oldState: BlockState,
        newState: BlockState,
    ) {
        update(chunk, blockPos, newState)
    }

    public fun burnoutFor(state: BlockState, current: Time?, now: Time): Burnout? {
        val block = state.block
        val _ = block.emitterOrNull() ?: return null
        val fuelled = block.fuelledOrNull() ?: return Burnout.NEVER
        if (!state.isLit()) return null
        val fuel = fuelled.getFuel(state)
        if (fuel.level == 0) return null
        return fuelled.getBurnout(current, now, fuel)
    }

    private fun Long2LongOpenHashMap.burnoutAt(key: Long): Time? {
        val existing = get(key)
        return if (existing == ABSENT) null else Time(existing)
    }

    private fun Long2LongOpenHashMap.write(key: Long, burnout: Burnout?): Boolean {
        return if (burnout == null) remove(key) != ABSENT
        else put(key, burnout.at.value) != burnout.at.value
    }

    public fun remove(chunk: LevelChunk, pos: BlockPos) {
        Survival.platform.emitters.get(chunk)?.remove(pos.asLong())
    }

    public fun tickDrops(level: ServerLevel) {
        val schedule = ServerState.dropSchedule(level)
        val now = Time(level.gameTime)
        for (chunkPos in schedule.due(now)) {
            val chunk = level.chunkSource.getChunkNow(chunkPos.x, chunkPos.z)
            if (chunk == null) {
                schedule.reset(chunkPos, null)
                continue
            }
            chunk.applyDrops(now)
            val index = Survival.platform.emitters.get(chunk)
            schedule.reset(chunkPos, index?.let { chunk.earliestDeadline(it, now) })
        }
    }

    public fun update(chunk: LevelChunk, blockPos: BlockPos, state: BlockState) {
        val level = chunk.level
        if (level.isClientSide) return
        val now = Time(level.gameTime)
        val key = blockPos.asLong()
        val existing = Survival.platform.emitters.get(chunk)
        val burnout = burnoutFor(state, existing?.burnoutAt(key), now)
        if (existing == null && burnout == null) return
        val index = existing ?: create()
        if (!index.write(key, burnout)) return
        Survival.platform.emitters.set(chunk, index)
        dropDeadline(state, burnout, now)?.let {
            ServerState.dropSchedule(level).notice(chunk.pos, it)
        }
    }

    private fun dropDeadline(state: BlockState, burnout: Burnout?, now: Time): Time? {
        if (burnout == null || burnout == Burnout.NEVER) return null
        val fuelled = state.block.fuelledOrNull() ?: return null
        val stateIsBehind = burnout.fuelAt(now, Fuel.MAX, fuelled.burnRate) < fuelled.getFuel(state)
        if (stateIsBehind) return now
        return burnout.nextDropAt(now, Fuel.MAX, fuelled.burnRate)
    }

    private fun reconciledBurnout(state: BlockState, stored: Time?, now: Time): Burnout? {
        val derived = burnoutFor(state, stored, now) ?: return null
        if (derived == Burnout.NEVER || stored == null) return derived
        return Burnout(stored)
    }

    public fun encode(map: Long2LongOpenHashMap): LongStream {
        val flat = LongArray(map.size * 2)
        var i = 0
        val entries = Long2LongMaps.fastIterator(map)
        while (entries.hasNext()) {
            val entry = entries.next()
            flat[i++] = entry.longKey
            flat[i++] = entry.longValue
        }
        return Arrays.stream(flat)
    }

    public fun decode(stream: LongStream): DataResult<Long2LongOpenHashMap> {
        val flat = stream.toArray()
        if (flat.size % 2 != 0) return DataResult.error { "Malformed emitter data, length is ${flat.size} but should be even" }
        val map = create(flat.size / 2)
        var i = 0
        while (i < flat.size) {
            map.put(flat[i], flat[i + 1])
            i += 2
        }
        return DataResult.success(map)
    }

    public fun read(buf: ByteBuf): Long2LongOpenHashMap {
        val size = VarInt.read(buf)
        val map = create(size)
        repeat(size) {
            val pos = buf.readLong()
            val at = buf.readLong()
            map.put(pos, at)
        }
        return map
    }

    public fun write(buf: ByteBuf, map: Long2LongOpenHashMap) {
        VarInt.write(buf, map.size)
        val entries = Long2LongMaps.fastIterator(map)
        while (entries.hasNext()) {
            val entry = entries.next()
            buf.writeLong(entry.longKey)
            buf.writeLong(entry.longValue)
        }
    }

    public fun LevelChunk.applyDrops(now: Time) {
        val index = Survival.platform.emitters.get(this) ?: return
        for (key in index.keys.toLongArray()) {
            val at = index.burnoutAt(key) ?: continue
            val pos = BlockPos.of(key)
            val state = getBlockState(pos)
            val fuelled = state.block.fuelledOrNull() ?: continue
            val wanted = Burnout(at).fuelAt(now, Fuel.MAX, fuelled.burnRate)
            if (wanted == fuelled.getFuel(state)) continue
            val drained = fuelled.setFuel(state, wanted)
            val next = if (wanted.level == 0) fuelled.exhausted(drained) else (drained)
            level.setBlock(pos, next, Block.UPDATE_CLIENTS)
        }
    }

    public fun LevelChunk.earliestDeadline(index: Long2LongOpenHashMap, now: Time): Time? {
        var earliest: Time? = null
        val keys = index.keys.iterator()
        while (keys.hasNext()) {
            val key = keys.nextLong()
            val at = index.burnoutAt(key) ?: continue
            val deadline =
                dropDeadline(getBlockState(BlockPos.of(key)), Burnout(at), now) ?: continue
            val current = earliest
            if (current == null || deadline < current) earliest = deadline
        }
        return earliest
    }

    public fun LevelChunk.pruneMissing(index: Long2LongOpenHashMap): Boolean {
        val keys = index.keys.iterator()
        var didPrune = false
        while (keys.hasNext()) {
            val key = keys.nextLong()
            if (getBlockState(BlockPos.of(key)).block.emitterOrNull() == null) {
                keys.remove()
                didPrune = true
            }
        }
        return didPrune
    }

    public fun LevelChunk.reconcileEmitters(): Time? {
        if (level.isClientSide) return null
        val now = Time(level.gameTime)
        val existing = Survival.platform.emitters.get(this)
        val index = existing ?: create()
        val didPrune = pruneMissing(index)
        var earliest: Time? = null
        var didUpdate = didPrune
        for (sectionY in minSectionY..maxSectionY) {
            val section = getSection(getSectionIndexFromSectionY(sectionY))
            if (!section.maybeHas { state -> state.block.emitterOrNull() != null }) continue
            val originX = pos.minBlockX
            val originY = SectionPos.sectionToBlockCoord(sectionY)
            val originZ = pos.minBlockZ
            for (localY in 0..15) {
                for (localZ in 0..15) {
                    for (localX in 0..15) {
                        val blockPos =
                            BlockPos(originX + localX, originY + localY, originZ + localZ)
                        val state = section.getBlockState(localX, localY, localZ)
                        val key = blockPos.asLong()
                        val burnout = reconciledBurnout(state, index.burnoutAt(key), now)
                        val write = index.write(key, burnout)
                        didUpdate = didUpdate || write
                        val deadline = dropDeadline(state, burnout, now)
                        if (deadline != null) {
                            val current = earliest
                            if (current == null || deadline < current) earliest = deadline
                        }
                    }
                }
            }
        }
        if (existing != null) {
            if (didUpdate) markUnsaved()
        } else if (index.isNotEmpty()) {
            Survival.platform.emitters.set(this, index)
        }
        return earliest
    }

    public val CODEC: Codec<Long2LongOpenHashMap> =
        Codec.LONG_STREAM.comapFlatMap(::decode, ::encode)

    public val STREAM_CODEC: StreamCodec<ByteBuf, Long2LongOpenHashMap> =
        StreamCodec.of(::write, ::read)

}