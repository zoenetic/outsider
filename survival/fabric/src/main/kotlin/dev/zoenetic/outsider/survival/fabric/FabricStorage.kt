package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.platform.ChunkStore
import dev.zoenetic.outsider.survival.platform.PlayerStore
import dev.zoenetic.outsider.survival.platform.SyncedPlayerStore
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk

internal class FabricPersistentSyncedChunkStore<T : Any>(
    private val type: AttachmentType<T>,
) : ChunkStore<T> {
    override fun get(chunk: LevelChunk): T? = chunk.getAttached(type)
    override fun set(chunk: LevelChunk, value: T) {
        chunk.setAttached(type, value)
    }
    override fun remove(chunk: LevelChunk) {
        chunk.removeAttached(type)
    }
}

internal class FabricPlayerStore<T : Any>(
    private val type: AttachmentType<T>,
) : PlayerStore<T> {
    override fun get(player: ServerPlayer): T? = player.getAttached(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setAttached(type, value)
    }
}

internal class FabricPersistentSyncedPlayerStore<T : Any>(
    private val type: AttachmentType<T>,
) : SyncedPlayerStore<T> {
    override fun get(player: Player): T? = player.getAttached(type)
    override fun set(player: Player, value: T) {
        player.setAttached(type, value)
    }
}
