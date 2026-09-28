package dev.zoenetic.outsider.survival.neoforge

import dev.zoenetic.outsider.survival.platform.ChunkStore
import dev.zoenetic.outsider.survival.platform.PlayerStore
import dev.zoenetic.outsider.survival.platform.SyncedPlayerStore
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.neoforge.attachment.AttachmentType
import java.util.function.Supplier

internal class NeoForgePersistentSyncedChunkStore<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : ChunkStore<T> {
    override fun get(chunk: LevelChunk): T? = chunk.getExistingDataOrNull(type)
    override fun set(chunk: LevelChunk, value: T) {
        chunk.setData(type, value)
    }
    override fun remove(chunk: LevelChunk) {
        chunk.removeData(type)
    }
}

internal class NeoForgePlayerStore<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : PlayerStore<T> {
    override fun get(player: ServerPlayer): T? = player.getExistingDataOrNull(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setData(type, value)
    }
}

internal class NeoForgePersistentSyncedPlayerStore<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : SyncedPlayerStore<T> {
    override fun get(player: Player): T? = player.getExistingDataOrNull(type)
    override fun set(player: Player, value: T) {
        player.setData(type, value)
    }
}
