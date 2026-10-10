package dev.zoenetic.outsider.survival.platform

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk

public interface ChunkStore<T> {
    public fun get(chunk: LevelChunk): T?
    public fun set(chunk: LevelChunk, value: T)
    public fun remove(chunk: LevelChunk)
}

public interface PlayerStore<T> {
    public fun get(player: ServerPlayer): T?
    public fun set(player: ServerPlayer, value: T)
}

public interface SyncedPlayerStore<T> {
    public fun get(player: Player): T?
    public fun set(player: Player, value: T)
}
