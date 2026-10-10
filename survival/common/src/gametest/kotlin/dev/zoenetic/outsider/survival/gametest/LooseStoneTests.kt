package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.OutsiderLooseStones
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangement
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.STONES
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.WATERLOGGED
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.toShape
import dev.zoenetic.outsider.survival.stone.loose.MAX_STONES
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes

private const val TEST_TICKS = 20

object LooseStoneTests {

    private val STONES_POS = BlockPos(1, 2, 1)

    fun looseStonesNeedASturdyFloor(helper: GameTestHelper) {
        val stones = OutsiderLooseStones.STONE.block.defaultBlockState()
        val pos = helper.absolutePos(STONES_POS)

        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        if (!stones.canSurvive(helper.level, pos)) {
            throw helper.assertionException("loose stones should survive on stone")
        }
        helper.setBlock(STONES_POS.below(), Blocks.OAK_FENCE)
        if (stones.canSurvive(helper.level, pos)) {
            throw helper.assertionException("loose stones should not survive on a fence")
        }
        helper.succeed()
    }

    fun looseStonesBreakWhenTheirSupportIsRemoved(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        helper.setBlock(STONES_POS, OutsiderLooseStones.STONE.block)
        helper.setBlock(STONES_POS.below(), Blocks.AIR)
        helper.assertBlockNotPresent(OutsiderLooseStones.STONE.block, STONES_POS)
        helper.succeed()
    }

    fun waterloggedLooseStonesHoldWater(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        helper.setBlock(
            STONES_POS,
            OutsiderLooseStones.STONE.block.defaultBlockState().setValue(WATERLOGGED, true),
        )
        val fluid = helper.getBlockState(STONES_POS).fluidState
        if (!fluid.`is`(Fluids.WATER)) {
            throw helper.assertionException("waterlogged loose stones should hold water: $fluid")
        }
        helper.succeed()
    }

    // Indices worked out outside Minecraft (Mth.getSeed, then java.util.Random.nextInt over the
    // 576 layouts), so any change to how getShape picks breaks this. Every count shares a list
    // length, so a position picks the same layout index at every count.
    private val RENDERED_PICKS = mapOf(
        BlockPos(0, 64, 0) to 350,
        BlockPos(123, 64, -456) to 12,
        BlockPos(-7, 70, 9) to 258,
        BlockPos(1000, 80, 1000) to 259,
    )

    // Every loose stone block with the arrangement it was registered with.
    private val TYPES: List<Pair<Block, LooseStoneArrangement>> = listOf(
        OutsiderLooseStones.ANDESITE.block to LooseStoneArrangements.ANDESITE,
        OutsiderLooseStones.BASALT.block to LooseStoneArrangements.BASALT,
        OutsiderLooseStones.BLACKSTONE.block to LooseStoneArrangements.BLACKSTONE,
        OutsiderLooseStones.CALCITE.block to LooseStoneArrangements.CALCITE,
        OutsiderLooseStones.DEEPSLATE.block to LooseStoneArrangements.DEEPSLATE,
        OutsiderLooseStones.DIORITE.block to LooseStoneArrangements.DIORITE,
        OutsiderLooseStones.END_STONE.block to LooseStoneArrangements.END_STONE,
        OutsiderLooseStones.GRANITE.block to LooseStoneArrangements.GRANITE,
        OutsiderLooseStones.RED_SANDSTONE.block to LooseStoneArrangements.RED_SANDSTONE,
        OutsiderLooseStones.SANDSTONE.block to LooseStoneArrangements.SANDSTONE,
        OutsiderLooseStones.STONE.block to LooseStoneArrangements.STONE,
        OutsiderLooseStones.TUFF.block to LooseStoneArrangements.TUFF,
    )

    private fun outline(helper: GameTestHelper, block: Block, pos: BlockPos, stones: Int) =
        block.defaultBlockState().setValue(STONES, stones)
            .getShape(helper.level, pos, CollisionContext.empty())

    // Each block's outline must come from its own arrangement, not another type's.
    fun theOutlineFollowsTheRenderedArrangement(helper: GameTestHelper) {
        for ((block, arrangement) in TYPES) {
            for ((pos, pick) in RENDERED_PICKS) {
                for (stones in 1..MAX_STONES) {
                    val expected = arrangement.forCount(stones)[pick]
                        .map { it.toShape() }
                        .reduce(Shapes::or)
                    val outline = outline(helper, block, pos, stones)
                    helper.ensure(!Shapes.joinIsNotEmpty(outline, expected, BooleanOp.NOT_SAME)) {
                        "$stones of ${block.descriptionId} at $pos should outline layout $pick"
                    }
                }
            }
        }
        helper.succeed()
    }

    fun addingAStoneKeepsTheOthersInPlace(helper: GameTestHelper) {
        for ((block, _) in TYPES) {
            for (pos in RENDERED_PICKS.keys) {
                for (stones in 1..<MAX_STONES) {
                    val fewer = outline(helper, block, pos, stones)
                    val more = outline(helper, block, pos, stones + 1)
                    helper.ensure(!Shapes.joinIsNotEmpty(fewer, more, BooleanOp.ONLY_FIRST)) {
                        "adding a stone to ${block.descriptionId} at $pos moved the first $stones"
                    }
                }
            }
        }
        helper.succeed()
    }

    private fun placed(name: String): ResourceKey<PlacedFeature> = ResourceKey.create(
        Registries.PLACED_FEATURE,
        Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name),
    )

    // One biome per loose stone group, with the features its biome tag must give it. Catches a
    // group missing from FabricBiomeModifications or from the NeoForge biome modifiers.
    private val GROUP_FEATURES = mapOf(
        Biomes.PLAINS to listOf(
            "loose_temperate_stones_scatter",
            "loose_temperate_stones_near_plants",
            "loose_temperate_stones_underwater_scatter",
            "loose_temperate_stones_underwater_near_plants",
        ),
        Biomes.WINDSWEPT_HILLS to listOf(
            "loose_mountain_stones_scatter",
            "loose_mountain_stones_near_plants",
        ),
        Biomes.STONY_PEAKS to listOf(
            "loose_peak_stones_scatter",
            "loose_peak_stones_near_plants",
        ),
        Biomes.DESERT to listOf(
            "loose_desert_stones_scatter",
            "loose_desert_stones_near_plants",
        ),
        Biomes.BADLANDS to listOf(
            "loose_badlands_stones_scatter",
            "loose_badlands_stones_near_plants",
        ),
        Biomes.BEACH to listOf(
            "loose_beach_stones_scatter",
            "loose_beach_stones_near_plants",
        ),
        Biomes.RIVER to listOf(
            "loose_ocean_stones_underwater_scatter",
            "loose_ocean_stones_underwater_near_plants",
        ),
        Biomes.WARM_OCEAN to listOf(
            "loose_warm_ocean_stones_underwater_scatter",
            "loose_warm_ocean_stones_underwater_near_plants",
        ),
    ).mapValues { (_, names) -> names.map(::placed) }

    // The near-plants features only see vanilla's grass and seagrass if they run after it.
    fun eachGroupsFeaturesRunAfterVanillaVegetation(helper: GameTestHelper) {
        val biomes = helper.level.registryAccess().lookupOrThrow(Registries.BIOME)
        for ((biome, features) in GROUP_FEATURES) {
            val vegetal = biomes.getOrThrow(biome).value().generationSettings.features()[
                GenerationStep.Decoration.VEGETAL_DECORATION.ordinal,
            ].map { it.unwrapKey().orElseThrow() }
            val lastVanilla = vegetal.indexOfLast { it.identifier().namespace == "minecraft" }
            for (key in features) {
                val index = vegetal.indexOf(key)
                helper.ensure(index >= 0) { "${key.identifier()} is not in ${biome.identifier()}" }
                helper.ensure(index > lastVanilla) {
                    "${key.identifier()} runs before vanilla vegetation in ${biome.identifier()}"
                }
            }
        }
        helper.succeed()
    }

    fun placingLooseStonesStacksUpToFour(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        val oneTooMany = MAX_STONES + 1
        player.setItemInHand(
            InteractionHand.MAIN_HAND,
            ItemStack(OutsiderLooseStones.STONE.item, oneTooMany),
        )

        // Clicking the ground's top face targets the space above it, where the cluster sits.
        repeat(oneTooMany) {
            helper.placeAt(player, player.mainHandItem, STONES_POS.below(), Direction.UP)
        }

        val stones = helper.getBlockState(STONES_POS).getValue(STONES)
        if (stones != MAX_STONES) {
            throw helper.assertionException("expected $MAX_STONES stones, found $stones")
        }
        helper.assertBlockNotPresent(OutsiderLooseStones.STONE.block, STONES_POS.above())
        if (player.mainHandItem.count != 1) {
            throw helper.assertionException(
                "the fifth stone should stay in hand, found ${player.mainHandItem}",
            )
        }
        helper.succeed()
    }

    fun breakingAClusterDropsOneStonePerStone(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        val pos = helper.absolutePos(STONES_POS)
        for (stones in 1..MAX_STONES) {
            val state = OutsiderLooseStones.STONE.block.defaultBlockState().setValue(STONES, stones)
            val dropped = Block.getDrops(state, helper.level, pos, null)
                .filter { it.`is`(OutsiderLooseStones.STONE.item) }
                .sumOf { it.count }
            if (dropped != stones) {
                throw helper.assertionException("$stones stones dropped $dropped")
            }
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest(
            "placing_loose_stones_stacks_up_to_four",
            TEST_TICKS,
            ::placingLooseStonesStacksUpToFour,
        ),
        SurvivalTest(
            "breaking_a_cluster_drops_one_stone_per_stone",
            TEST_TICKS,
            ::breakingAClusterDropsOneStonePerStone,
        ),
        SurvivalTest(
            "each_groups_features_run_after_vanilla_vegetation",
            TEST_TICKS,
            ::eachGroupsFeaturesRunAfterVanillaVegetation,
        ),
        SurvivalTest(
            "adding_a_stone_keeps_the_others_in_place",
            TEST_TICKS,
            ::addingAStoneKeepsTheOthersInPlace,
        ),
        SurvivalTest(
            "the_outline_follows_the_rendered_arrangement",
            TEST_TICKS,
            ::theOutlineFollowsTheRenderedArrangement,
        ),
        SurvivalTest("loose_stones_need_a_sturdy_floor", TEST_TICKS, ::looseStonesNeedASturdyFloor),
        SurvivalTest(
            "loose_stones_break_when_their_support_is_removed",
            TEST_TICKS,
            ::looseStonesBreakWhenTheirSupportIsRemoved,
        ),
        SurvivalTest(
            "waterlogged_loose_stones_hold_water",
            TEST_TICKS,
            ::waterloggedLooseStonesHoldWater,
        ),
    )
}
