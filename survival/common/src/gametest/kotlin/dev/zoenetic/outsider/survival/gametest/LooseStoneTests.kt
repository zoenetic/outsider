package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.registry.blocks.OutsiderLooseStoneBlocks
import dev.zoenetic.outsider.survival.registry.items.OutsiderLooseStoneItems
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
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes

private const val TEST_TICKS = 20

object LooseStoneTests {

    private val STONES_POS = BlockPos(1, 2, 1)

    fun looseStonesNeedASturdyFloor(helper: GameTestHelper) {
        val stones = OutsiderLooseStoneBlocks.STONE.defaultBlockState()
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
        helper.setBlock(STONES_POS, OutsiderLooseStoneBlocks.STONE)
        helper.setBlock(STONES_POS.below(), Blocks.AIR)
        helper.assertBlockNotPresent(OutsiderLooseStoneBlocks.STONE, STONES_POS)
        helper.succeed()
    }

    fun waterloggedLooseStonesHoldWater(helper: GameTestHelper) {
        helper.setBlock(STONES_POS.below(), Blocks.STONE)
        helper.setBlock(
            STONES_POS,
            OutsiderLooseStoneBlocks.STONE.defaultBlockState().setValue(WATERLOGGED, true),
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
        OutsiderLooseStoneBlocks.ANDESITE to LooseStoneArrangements.ANDESITE,
        OutsiderLooseStoneBlocks.BASALT to LooseStoneArrangements.BASALT,
        OutsiderLooseStoneBlocks.BLACKSTONE to LooseStoneArrangements.BLACKSTONE,
        OutsiderLooseStoneBlocks.CALCITE to LooseStoneArrangements.CALCITE,
        OutsiderLooseStoneBlocks.DEEPSLATE to LooseStoneArrangements.DEEPSLATE,
        OutsiderLooseStoneBlocks.DIORITE to LooseStoneArrangements.DIORITE,
        OutsiderLooseStoneBlocks.ENDSTONE to LooseStoneArrangements.ENDSTONE,
        OutsiderLooseStoneBlocks.GRANITE to LooseStoneArrangements.GRANITE,
        OutsiderLooseStoneBlocks.RED_SANDSTONE to LooseStoneArrangements.RED_SANDSTONE,
        OutsiderLooseStoneBlocks.SANDSTONE to LooseStoneArrangements.SANDSTONE,
        OutsiderLooseStoneBlocks.STONE to LooseStoneArrangements.STONE,
        OutsiderLooseStoneBlocks.TUFF to LooseStoneArrangements.TUFF,
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
            ItemStack(OutsiderLooseStoneItems.STONE, oneTooMany),
        )

        // Clicking the ground's top face targets the space above it, where the cluster sits.
        repeat(oneTooMany) {
            helper.placeAt(player, player.mainHandItem, STONES_POS.below(), Direction.UP)
        }

        val stones = helper.getBlockState(STONES_POS).getValue(STONES)
        if (stones != MAX_STONES) {
            throw helper.assertionException("expected $MAX_STONES stones, found $stones")
        }
        helper.assertBlockNotPresent(OutsiderLooseStoneBlocks.STONE, STONES_POS.above())
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
            val state = OutsiderLooseStoneBlocks.STONE.defaultBlockState().setValue(STONES, stones)
            val dropped = Block.getDrops(state, helper.level, pos, null)
                .filter { it.`is`(OutsiderLooseStoneItems.STONE) }
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
