package dev.zoenetic.outsider.survival.neoforge

import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.emission.isSendable
import dev.zoenetic.outsider.survival.platform.ChunkStore
import dev.zoenetic.outsider.survival.platform.Platform
import dev.zoenetic.outsider.survival.platform.PlayerStore
import dev.zoenetic.outsider.survival.platform.SyncedPlayerStore
import dev.zoenetic.outsider.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.attachment.AttachmentType
import java.util.function.Supplier

public object NeoForgePlatform : Platform {
    override val name: String = "neoforge"

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.getCurrent().isProduction

    override fun isModLoaded(modId: String): Boolean =
        FMLLoader.getCurrent().getLoadingModList()
            .getModFileById(modId) != null

    override val register: NeoForgeRegister = NeoForgeRegister
    override val wideners: NeoForgeWidener = NeoForgeWidener

    override val emitters: ChunkStore<Long2LongOpenHashMap> = NeoForgePersistentSyncedChunkStore(
        register.attachment("chunk_emitters") {
            AttachmentType.builder(Supplier { EmitterIndex.create() })
                .serialize(EmitterIndex.CODEC.fieldOf("emitters"))
                .sync({ chunk, _ ->
                    chunk is LevelChunk && chunk.isSendable()
                }, EmitterIndex.STREAM_CODEC)
                .build()
        }
    )

    override val playerConditions: PlayerStore<PlayerConditions> =
        NeoForgePlayerStore(
            register.attachment("player_conditions") {
                AttachmentType.builder(Supplier { PlayerConditions.EMPTY }).build()
            }
        )

    override val vitals: SyncedPlayerStore<Vitals> = NeoForgePersistentSyncedPlayerStore(
        register.attachment("vitals") {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        }
    )

    public fun init(bus: IEventBus) {
        register.init(bus)
        wideners.init(bus)
    }
}