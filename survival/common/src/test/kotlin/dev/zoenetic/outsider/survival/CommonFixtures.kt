package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.climate.temperatureFromNoise
import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.platform.*
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.SharedConstants
import net.minecraft.core.*
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.resources.Identifier
import net.minecraft.resources.RegistryFixedCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerChunkCache
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.MultiNoiseBiomeSource
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.level.chunk.PalettedContainerFactory
import net.minecraft.world.level.chunk.Strategy
import net.minecraft.world.level.levelgen.DensityFunction
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.phys.AABB
import org.mockito.Mockito.*
import java.util.*
import java.util.function.Supplier

object TestRegistry : Register {
    private fun id(name: String) = Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)

    override fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block> = Holder.direct(mock(Block::class.java))

    override fun blockEntity(
        name: String,
        blocksFactory: () -> Set<Block>,
        entityFactory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
    ): Holder<BlockEntityType<*>> = Holder.direct(mock(BlockEntityType::class.java))

    override fun blockItem(
        name: String,
        blockFactory: () -> Block,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = Holder.direct(mock(Item::class.java))

    override fun <T : Any> component(
        name: String,
        builder: DataComponentType.Builder<T>,
    ): Supplier<DataComponentType<T>> = Supplier { mock<DataComponentType<T>>() }

    override fun item(
        name: String,
        itemFactory: (Item.Properties) -> Item,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = Holder.direct(mock(Item::class.java))

    override fun sound(name: String, factory: (Identifier) -> SoundEvent): Holder<SoundEvent> =
        Holder.direct(factory(id(name)))

    override fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = Holder.direct(mock(Item::class.java))
}

object TestWidener : Widener {
    private val requests: MutableList<Pair<BlockEntityType<*>, List<Block>>> = mutableListOf()

    override fun addValidBlocks(type: BlockEntityType<*>, blocks: () -> List<Block>) {
        requests += type to blocks()
    }

    fun widened(type: BlockEntityType<*>): List<Block> =
        requests.filter { it.first === type }.flatMap { it.second }

    fun reset(): Unit = requests.clear()
}

object TestPlatform : Platform {
    override val name: String = "test"
    override val isDevelopmentEnvironment: Boolean = false
    override fun isModLoaded(modId: String): Boolean = false

    override val register: Register = TestRegistry
    override val wideners: Widener = TestWidener

    override val emitters: ChunkStore<Long2LongOpenHashMap> =
        object : ChunkStore<Long2LongOpenHashMap> {
            private val byChunk = IdentityHashMap<LevelChunk, Long2LongOpenHashMap>()

            override fun get(chunk: LevelChunk): Long2LongOpenHashMap =
                byChunk.getOrPut(chunk) { EmitterIndex.create() }

            override fun set(chunk: LevelChunk, value: Long2LongOpenHashMap) {
                byChunk[chunk] = value
            }

            override fun remove(chunk: LevelChunk) {
                byChunk.remove(chunk)
            }
        }

    override val playerConditions: PlayerStore<PlayerConditions> =
        object : PlayerStore<PlayerConditions> {
            private val byPlayer = IdentityHashMap<ServerPlayer, PlayerConditions>()
            override fun get(player: ServerPlayer): PlayerConditions? = byPlayer[player]

            override fun set(player: ServerPlayer, value: PlayerConditions) {
                byPlayer[player] = value
            }
        }

    override val vitals: SyncedPlayerStore<Vitals> =
        object : SyncedPlayerStore<Vitals> {
            private val byPlayer = IdentityHashMap<Player, Vitals>()
            override fun get(player: Player): Vitals? = byPlayer[player]

            override fun set(player: Player, value: Vitals) {
                byPlayer[player] = value
            }
        }
}

object CommonFixtures {
    init {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        Survival.init(TestPlatform)
    }

    fun bootstrap() {
        // called to initialise the fixtures
    }

    private val itemComponentsBound: Unit by lazy {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach { it.apply() }
    }

    fun bindItemComponents(): Unit = itemComponentsBound

    const val MIN_Y = -64
    const val HEIGHT = 384

    private val lookup: HolderLookup.Provider by lazy { VanillaRegistries.createLookup() }

    private val containerFactory: PalettedContainerFactory by lazy {
        val blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)
        val air = Blocks.AIR.defaultBlockState()
        val plains: Holder<Biome> = lookup.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)
        val biomeIds = IdMapper<Holder<Biome>>().apply { add(plains) }
        val biomeStrategy = Strategy.createForBiomes(biomeIds)
        PalettedContainerFactory(
            blockStrategy,
            air,
            PalettedContainer.codecRW(BlockState.CODEC, blockStrategy, air),
            biomeStrategy,
            plains,
            PalettedContainer.codecRO(
                RegistryFixedCodec.create(Registries.BIOME),
                biomeStrategy,
                plains,
            ),
        )
    }

    fun fakeLevel(): Level {
        val level = mock(Level::class.java, CALLS_REAL_METHODS)
        doReturn(HEIGHT).`when`(level).height
        doReturn(MIN_Y).`when`(level).minY
        doReturn(false).`when`(level).isClientSide
        doReturn(0L).`when`(level).gameTime
        doReturn(containerFactory).`when`(level).palettedContainerFactory()
        return level
    }

    fun chunk(level: Level, pos: ChunkPos = ChunkPos(16, 32)): LevelChunk = LevelChunk(level, pos)

    fun fakeServerLevel(): ServerLevel {
        val level = mock(ServerLevel::class.java, CALLS_REAL_METHODS)
        doReturn(HEIGHT).`when`(level).height
        doReturn(MIN_Y).`when`(level).minY
        doReturn(false).`when`(level).isClientSide
        doReturn(containerFactory).`when`(level).palettedContainerFactory()
        return level
    }

    const val SEED: Long = 1234L
    const val SEA_LEVEL: Int = 63

    val randomState: RandomState by lazy {
        RandomState.create(lookup, NoiseGeneratorSettings.OVERWORLD, SEED)
    }

    fun climateLevel(): ServerLevel {
        val level = fakeServerLevel()
        val chunkSource = mock(ServerChunkCache::class.java)
        doReturn(randomState).`when`(chunkSource).randomState()
        doReturn(chunkSource).`when`(level).chunkSource
        doReturn(SEA_LEVEL).`when`(level).seaLevel
        return level
    }

    fun climateChunk(pos: ChunkPos, level: ServerLevel = climateLevel()): LevelChunk =
        LevelChunk(level, pos)

    data class ClimateSite(val pos: ChunkPos, val noise: Double, val temperature: Heat)

    private fun temperatureNoiseAt(pos: ChunkPos): Double =
        randomState.sampler().temperature().compute(
            DensityFunction.SinglePointContext(pos.middleBlockX, SEA_LEVEL, pos.middleBlockZ),
        )

    const val SCAN_RADIUS_CHUNKS: Int = 512
    const val SCAN_STEP_CHUNKS: Int = 8

    // this is deterministic for a given seed and Minecraft version, so tests can find extremes
    val climateScan: List<ClimateSite> by lazy {
        val sites = ArrayList<ClimateSite>()
        for (x in -SCAN_RADIUS_CHUNKS..SCAN_RADIUS_CHUNKS step SCAN_STEP_CHUNKS) {
            for (z in -SCAN_RADIUS_CHUNKS..SCAN_RADIUS_CHUNKS step SCAN_STEP_CHUNKS) {
                val pos = ChunkPos(x, z)
                val noise = temperatureNoiseAt(pos)
                sites.add(ClimateSite(pos, noise, temperatureFromNoise(noise)))
            }
        }
        sites
    }

    val hottestSite: ClimateSite by lazy { climateScan.maxBy { it.temperature } }
    val coldestSite: ClimateSite by lazy { climateScan.minBy { it.temperature } }

    private val biomeSource: MultiNoiseBiomeSource by lazy {
        MultiNoiseBiomeSource.createFromPreset(
            lookup.lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                .getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD),
        )
    }

    fun biomeAt(pos: ChunkPos): Holder<Biome> = biomeSource.getNoiseBiome(
        QuartPos.fromBlock(pos.middleBlockX),
        QuartPos.fromBlock(SEA_LEVEL),
        QuartPos.fromBlock(pos.middleBlockZ),
        randomState.sampler(),
    )

    fun nearestSiteIn(vararg wanted: ResourceKey<Biome>): ClimateSite? = climateScan
        .sortedBy { it.pos.x * it.pos.x + it.pos.z * it.pos.z }
        .firstOrNull { site -> wanted.any { biomeAt(site.pos).`is`(it) } }

    class FakeWorld(val level: ServerLevel, val requestedChunks: MutableList<ChunkPos>) {
        fun playerAt(pos: BlockPos): ServerPlayer {
            val player = mock(ServerPlayer::class.java)
            doReturn(level).`when`(player).level()
            doReturn(pos).`when`(player).blockPosition()
            doReturn(net.minecraft.world.phys.Vec3(pos.x + 0.5, pos.y + 1.62, pos.z + 0.5)).`when`(
                player,
            ).eyePosition
            doReturn(
                AABB.ofSize(
                    net.minecraft.world.phys.Vec3(
                        pos.x + 0.5,
                        pos.y.toDouble(),
                        pos.z + 0.5,
                    ),
                    0.6,
                    1.8,
                    0.6,
                ),
            ).`when`(player).boundingBox
            return player
        }
    }

    fun fakeWorld(skyBrightness: Int = 0, gameTime: Long = 0L, clockTime: Long = 0L): FakeWorld {
        val level = climateLevel()
        val requested = mutableListOf<ChunkPos>()
        val chunks = HashMap<Long, LevelChunk>()
        doAnswer { invocation ->
            val pos = ChunkPos(invocation.getArgument<Int>(0), invocation.getArgument<Int>(1))
            requested.add(pos)
            chunks.getOrPut(pos.pack()) { LevelChunk(level, pos) }
        }.`when`(level).getChunk(anyInt(), anyInt())
        doReturn(skyBrightness).`when`(level).getBrightness(any(LightLayer::class.java), any())
        doReturn(skyBrightness > 0).`when`(level).canSeeSky(any())
        doReturn(gameTime).`when`(level).gameTime
        doReturn(clockTime).`when`(level).overworldClockTime
        return FakeWorld(level, requested)
    }

    fun seaLevelCentreOf(pos: ChunkPos): BlockPos =
        BlockPos(pos.middleBlockX, SEA_LEVEL, pos.middleBlockZ)
}
