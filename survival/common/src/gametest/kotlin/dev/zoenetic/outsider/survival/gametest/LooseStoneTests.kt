package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.OutsiderBlocks
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.MAX_STONES
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangements.forCount
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.STONES
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.WATERLOGGED
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock.Companion.toShape
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
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes

private const val TEST_TICKS = 20

object LooseStoneTests {

    private val STONES_POS = BlockPos(1, 2, 1)

    fun looseStonesNeedASturdyFloor(helper: GameTestHelper) {
        val stones = OutsiderBlocks.LOOSE_STONE.defaultBlockState()
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
        helper.setBlock(STONES_POS, OutsiderBlocks.LOOSE_STONE)
        helper.setBlock(STONES_POS.below(), Blocks.AIR)
        helper.assertBlockNotPresent(OutsiderBlocks.LOOSE_STONE, STONES_POS)
        helper.succeed()
    }

    fun waterloggedLooseStonesHoldWater(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        helper.setBlock(
            STONES_POS,
            OutsiderBlocks.LOOSE_STONE.defaultBlockState().setValue(WATERLOGGED, true),
        )
        val fluid = helper.getBlockState(STONES_POS).fluidState
        if (!fluid.`is`(Fluids.WATER)) {
            throw helper.assertionException("waterlogged loose stones should hold water: $fluid")
        }
        helper.succeed()
    }

    // Indices worked out outside Minecraft (Mth.getSeed, then java.util.Random.nextInt over the
    // 16 / 72 / 96 / 24 arrangements), so any change to how getShape picks breaks this.
    private val RENDERED_PICKS = mapOf(
        BlockPos(0, 64, 0) to listOf(15, 62, 62, 14),
        BlockPos(123, 64, -456) to listOf(0, 12, 12, 12),
        BlockPos(-7, 70, 9) to listOf(2, 42, 66, 18),
        BlockPos(1000, 80, 1000) to listOf(4, 43, 67, 19),
    )

    fun theOutlineFollowsTheRenderedArrangement(helper: GameTestHelper) {
        for ((pos, picks) in RENDERED_PICKS) {
            for ((countIndex, pick) in picks.withIndex()) {
                val stones = countIndex + 1
                val state = OutsiderBlocks.LOOSE_STONE.defaultBlockState().setValue(STONES, stones)
                val outline = state.getShape(helper.level, pos, CollisionContext.empty())
                val expected = forCount(stones)[pick].map { it.toShape() }.reduce(Shapes::or)
                if (Shapes.joinIsNotEmpty(outline, expected, BooleanOp.NOT_SAME)) {
                    throw helper.assertionException(
                        "$stones stones at $pos should outline arrangement $pick",
                    )
                }
            }
        }
        helper.succeed()
    }

    private val PLACED_FEATURES = listOf(
        "loose_stone_scatter",
        "loose_stone_near_plants",
        "loose_stone_underwater_scatter",
        "loose_stone_underwater_near_plants",
    ).map { name ->
        ResourceKey.create(
            Registries.PLACED_FEATURE,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name),
        )
    }

    // The near-plants features only see vanilla's grass and seagrass if they run after it.
    fun theFeaturesRunAfterVanillaVegetationInPlains(helper: GameTestHelper) {
        val plains = helper.level.registryAccess().lookupOrThrow(Registries.BIOME)
            .getOrThrow(Biomes.PLAINS).value()
        val vegetal = plains.generationSettings.features()[
            GenerationStep.Decoration.VEGETAL_DECORATION.ordinal,
        ].map { it.unwrapKey().orElseThrow() }
        val lastVanilla = vegetal.indexOfLast { it.identifier().namespace == "minecraft" }
        for (key in PLACED_FEATURES) {
            val index = vegetal.indexOf(key)
            if (index < 0) throw helper.assertionException("${key.identifier()} is not in plains")
            if (index < lastVanilla) {
                throw helper.assertionException(
                    "${key.identifier()} runs before vanilla vegetation",
                )
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
            ItemStack(OutsiderItems.LOOSE_STONE, oneTooMany),
        )

        // Clicking the ground's top face targets the space above it, where the cluster sits.
        repeat(oneTooMany) {
            helper.placeAt(player, player.mainHandItem, STONES_POS.below(), Direction.UP)
        }

        val stones = helper.getBlockState(STONES_POS).getValue(STONES)
        if (stones != MAX_STONES) {
            throw helper.assertionException("expected $MAX_STONES stones, found $stones")
        }
        helper.assertBlockNotPresent(OutsiderBlocks.LOOSE_STONE, STONES_POS.above())
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
            val state = OutsiderBlocks.LOOSE_STONE.defaultBlockState().setValue(STONES, stones)
            val dropped = Block.getDrops(state, helper.level, pos, null)
                .filter { it.`is`(OutsiderItems.LOOSE_STONE) }
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
            "the_features_run_after_vanilla_vegetation_in_plains",
            TEST_TICKS,
            ::theFeaturesRunAfterVanillaVegetationInPlains,
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
