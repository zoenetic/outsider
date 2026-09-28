package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.Survival.MOD_ID
import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.platform.ChunkStore
import dev.zoenetic.outsider.survival.platform.Platform
import dev.zoenetic.outsider.survival.platform.PlayerStore
import dev.zoenetic.outsider.survival.platform.SyncedPlayerStore
import dev.zoenetic.outsider.survival.platform.Widener
import dev.zoenetic.outsider.survival.emission.isSendable
import dev.zoenetic.outsider.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier
import net.minecraft.world.level.chunk.LevelChunk

public object FabricPlatform : Platform {
    override val name: String = "fabric"
    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    override val register: FabricRegister = FabricRegister
    override val wideners: Widener = FabricWidener

    override val emitters: ChunkStore<Long2LongOpenHashMap> = FabricPersistentSyncedChunkStore(
        AttachmentRegistry.create(id("chunk_emitters")) {
            it.initializer { EmitterIndex.create() }
                .persistent(EmitterIndex.CODEC)
                .syncWith(EmitterIndex.STREAM_CODEC
                ) { chunk, _ ->
                    chunk is LevelChunk && chunk.isSendable()
                }
        }
    )

    override val playerConditions: PlayerStore<PlayerConditions> = FabricPlayerStore(
        AttachmentRegistry.create(id("player_conditions"))
    )

    override val vitals: SyncedPlayerStore<Vitals> = FabricPersistentSyncedPlayerStore(
        AttachmentRegistry.create(id("vitals"))
        {
            it.initializer { Vitals.DEFAULT }
                .persistent(Vitals.CODEC)
                .syncWith(Vitals.STREAM_CODEC, AttachmentSyncPredicate.all())
        })

}

private fun id(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)
