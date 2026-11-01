package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.climate.getHumidity
import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderBlocks
import dev.zoenetic.outsider.survival.units.Heat
import dev.zoenetic.outsider.survival.units.Time
import dev.zoenetic.outsider.survival.vitals.BodyTemperature
import dev.zoenetic.outsider.survival.vitals.MET
import dev.zoenetic.outsider.survival.vitals.Vitals
import dev.zoenetic.outsider.survival.vitals.conductance
import dev.zoenetic.outsider.survival.vitals.environment
import dev.zoenetic.outsider.survival.vitals.getContacts
import dev.zoenetic.outsider.survival.vitals.getInsulation
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.math.abs

data class SurvivalTest(val name: String, val maxTicks: Int, val run: (GameTestHelper) -> Unit)

private const val STILL_RATE = 1e-4
private const val STILL_DRIFT = 0.01

object SurvivalTests {

    private fun <T : Any> GameTestHelper.require(value: T?, what: String): T =
        value ?: throw assertionException("expected $what to be in the store, found nothing")

    private fun GameTestHelper.sample(player: ServerPlayer): PlayerConditions =
        PlayerConditions.getNew(player, Time(level.gameTime))

    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.playerAt(relative: BlockPos): ServerPlayer {
        val player = makeMockServerPlayerInLevel()
        val absolute = absolutePos(relative)
        player.snapTo(absolute.x + 0.5, absolute.y.toDouble(), absolute.z + 0.5)
        return player
    }

    private fun litCampfire(): BlockState =
        OutsiderBlocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, true)

    fun aRealServerLevelIsAvailable(helper: GameTestHelper) {
        check(!helper.level.isClientSide) { "expected a server level" }
        check(Survival.platform.name in setOf("fabric", "neoforge")) {
            "expected a real loader platform, got ${Survival.platform.name}"
        }
        helper.succeed()
    }

    fun conditionsComeFromThePlayersOwnChunk(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        val conditions = helper.sample(player)
        val ownChunk = helper.level.getChunkAt(player.blockPosition())
        val expected = ownChunk.getHumidity()

        if (conditions.humidity != expected) {
            throw helper.assertionException(
                "conditions read humidity ${conditions.humidity.value}, but the player's own " +
                    "chunk ${ownChunk.pos} reads ${expected.value}",
            )
        }
        helper.succeed()
    }

    fun conditionsRecordTheCurrentGameTime(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        val conditions = helper.sample(player)
        if (conditions.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "conditions recorded tick ${conditions.time.value}, level is at ${helper.level.gameTime}",
            )
        }
        helper.succeed()
    }

    fun theLevelTickStoresConditionsForPlayersInTheWorld(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        PlayerConditions.tick(helper.level)

        val stored = helper.require(
            Survival.platform.conditions.get(player),
            "conditions for a player the level tick has just visited",
        )
        if (stored.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "after a level tick the player's stored conditions are at tick " +
                    "${stored.time.value}, level is at ${helper.level.gameTime}",
            )
        }
        helper.succeed()
    }

    fun theProductionLoopDrivesBodyTemperature(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        var ambient: Heat? = null
        var start: Double? = null
        var predicted: Double? = null

        helper.startSequence()
            .thenIdle(2)
            .thenExecute {
                val conditions = helper.sample(player)
                val body = helper.require(Vitals.get(player), "seeded vitals").bodyTemperature.heat
                val contacts = player.getContacts(conditions)
                ambient = conditions.ambient
                start = body.celsius
                predicted = BodyTemperature.rate(
                    body,
                    contacts.environment(),
                    contacts.conductance(),
                    player.getInsulation(),
                    MET(1.0),
                )
            }
            .thenExecuteFor(100) { /* the mod's own tick handlers do the work */ }
            .thenExecute {
                val began = helper.require(start, "a captured starting temperature")
                val outside = helper.require(ambient, "a captured ambient temperature")
                val now = helper.require(Vitals.get(player), "vitals")
                    .bodyTemperature.heat.celsius
                val drift = now - began
                val where = "ambient ${outside.celsius}C, body $began -> $now (drift $drift)"
                val rate = helper.require(predicted, "a predicted rate")
                if (abs(rate) < STILL_RATE) {
                    if (abs(drift) > STILL_DRIFT) {
                        throw helper.assertionException(
                            "the model predicts a steady body, but it moved: $where",
                        )
                    }
                } else if (drift * rate <= 0.0) {
                    throw helper.assertionException(
                        "the model predicts ${if (rate > 0) "warming" else "cooling"}: $where",
                    )
                }
            }
            .thenSucceed()
    }

    fun placingACampfireRegistersAHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, litCampfire())

        val absolute = helper.absolutePos(relative)
        val index = Survival.platform.emitters.get(helper.level.getChunkAt(absolute))
            ?: throw helper.assertionException(
                "chunk at $absolute has no heat index at all (LevelChunkMixin?)",
            )
        if (!index.containsKey(absolute.asLong())) {
            throw helper.assertionException(
                "campfire at $absolute never reached the chunk's heat index (LevelChunkMixin?)",
            )
        }
        helper.succeed()
    }

    fun breakingACampfireDeregistersTheHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, litCampfire())
        helper.setBlock(relative, Blocks.AIR)

        val absolute = helper.absolutePos(relative)
        val index = Survival.platform.emitters.get(helper.level.getChunkAt(absolute))
        if (index != null && index.containsKey(absolute.asLong())) {
            throw helper.assertionException("campfire at $absolute is still in the heat index")
        }
        helper.succeed()
    }

    // A tag entry that doesn't resolve drops the whole tag, and only logs an error.
    fun theCampfireIsInTheCampfiresTag(helper: GameTestHelper) {
        helper.ensure(OutsiderBlocks.CAMPFIRE.defaultBlockState().`is`(BlockTags.CAMPFIRES)) {
            "outsider_survival:campfire isn't in #minecraft:campfires"
        }
        helper.ensure(Blocks.CAMPFIRE.defaultBlockState().`is`(BlockTags.CAMPFIRES)) {
            "minecraft:campfire isn't in #minecraft:campfires"
        }
        helper.succeed()
    }

    fun aNearbyCampfireIsFoundAsAHeatSource(helper: GameTestHelper) {
        val campfire = BlockPos(1, 1, 1)
        helper.setBlock(campfire, litCampfire())
        val player = helper.playerAt(BlockPos(2, 2, 1))

        val absolute = helper.absolutePos(campfire)
        val sources = EmitterIndex.atPlayer(player)
        if (sources.none { it == absolute }) {
            throw helper.assertionException(
                "campfire at $absolute was not among the ${sources.size} sources found " +
                    "around the player at ${player.blockPosition()}",
            )
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest("a_real_server_level_is_available", 20, ::aRealServerLevelIsAvailable),
        SurvivalTest(
            "conditions_come_from_the_players_own_chunk",
            20,
            ::conditionsComeFromThePlayersOwnChunk,
        ),
        SurvivalTest(
            "conditions_record_the_current_game_time",
            20,
            ::conditionsRecordTheCurrentGameTime,
        ),
        SurvivalTest(
            "the_level_tick_stores_conditions",
            20,
            ::theLevelTickStoresConditionsForPlayersInTheWorld,
        ),
        SurvivalTest(
            "the_production_loop_drives_body_temperature",
            200,
            ::theProductionLoopDrivesBodyTemperature,
        ),
        SurvivalTest(
            "placing_a_campfire_registers_a_heat_source",
            20,
            ::placingACampfireRegistersAHeatSource,
        ),
        SurvivalTest(
            "breaking_a_campfire_deregisters_it",
            20,
            ::breakingACampfireDeregistersTheHeatSource,
        ),
        SurvivalTest("a_nearby_campfire_is_found", 20, ::aNearbyCampfireIsFoundAsAHeatSource),
        SurvivalTest(
            "the_campfire_is_in_the_campfires_tag",
            20,
            ::theCampfireIsInTheCampfiresTag,
        ),
    ) + TorchLootTests.ALL + TorchLightTests.ALL + LooseStoneTests.ALL + RefuelTests.ALL +
        HandDrillTests.ALL + PlantFibreTests.ALL + CampfireBreakingTests.ALL
}
