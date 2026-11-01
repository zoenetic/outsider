package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState

private const val TEST_TICKS = 20

// Enough breaks that a 1 in 4 drop all but certainly shows up, and lands near its rate.
private const val BREAKS = 400

object PlantFibreTests {

    private val PLANT_POS = BlockPos(1, 2, 1)

    private fun GameTestHelper.fibreFrom(state: BlockState, tool: ItemStack): Int {
        setBlock(PLANT_POS.below(), Blocks.GRASS_BLOCK)
        setBlock(PLANT_POS, state)
        val pos = absolutePos(PLANT_POS)
        return (1..BREAKS).sumOf { _ ->
            Block.getDrops(state, level, pos, null, null, tool)
                .filter { it.`is`(OutsiderItems.PLANT_FIBRE) }
                .sumOf { it.count }
        }
    }

    private fun GameTestHelper.ensureDropsFibreByHand(state: BlockState) {
        val fibre = fibreFrom(state, ItemStack.EMPTY)
        ensure(fibre in BREAKS / 8..BREAKS * 3 / 8) {
            "$fibre fibre from $BREAKS breaks of ${state.block}, expected about ${BREAKS / 4}"
        }
    }

    fun shortGrassDropsPlantFibre(helper: GameTestHelper) {
        helper.ensureDropsFibreByHand(Blocks.SHORT_GRASS.defaultBlockState())
        helper.succeed()
    }

    fun fernsDropPlantFibre(helper: GameTestHelper) {
        helper.ensureDropsFibreByHand(Blocks.FERN.defaultBlockState())
        helper.succeed()
    }

    fun shearsNeverGetPlantFibre(helper: GameTestHelper) {
        val state = Blocks.SHORT_GRASS.defaultBlockState()
        val fibre = helper.fibreFrom(state, ItemStack(Items.SHEARS))
        helper.ensure(fibre == 0) { "shears got $fibre fibre from $BREAKS breaks" }
        val pos = helper.absolutePos(PLANT_POS)
        val drops = Block.getDrops(state, helper.level, pos, null, null, ItemStack(Items.SHEARS))
        helper.ensure(drops.any { it.`is`(Items.SHORT_GRASS) }) {
            "shears no longer get the grass: $drops"
        }
        helper.succeed()
    }

    private fun GameTestHelper.crafted(vararg items: Item): ItemStack {
        val input = CraftingInput.of(items.size, 1, items.map(::ItemStack))
        val recipe = level.server.recipeManager.getRecipeFor(RecipeType.CRAFTING, input, level)
        return recipe.map { it.value().assemble(input) }.orElse(ItemStack.EMPTY)
    }

    fun twoSticksMakeAHandDrill(helper: GameTestHelper) {
        val result = helper.crafted(Items.STICK, Items.STICK)
        helper.ensure(result.`is`(OutsiderItems.HAND_DRILL)) { "two sticks made $result" }
        helper.succeed()
    }

    private fun GameTestHelper.ensureMakesOneTinder(material: Item) {
        val result = crafted(material)
        ensure(result.`is`(OutsiderItems.TINDER) && result.count == 1) { "$material made $result" }
    }

    fun eachTinderMaterialMakesOneTinder(helper: GameTestHelper) {
        val materials = listOf(
            OutsiderItems.PLANT_FIBRE,
            Items.DEAD_BUSH,
            Items.DRY_SHORT_GRASS,
            Items.DRY_TALL_GRASS,
            Items.WHEAT,
            Items.DRIED_KELP,
            Items.PAPER,
        )
        materials.forEach { helper.ensureMakesOneTinder(it) }
        helper.succeed()
    }

    fun otherPlantsDontMakeTinder(helper: GameTestHelper) {
        val result = helper.crafted(Items.SHORT_GRASS)
        helper.ensure(!result.`is`(OutsiderItems.TINDER)) { "short grass made $result" }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest("short_grass_drops_plant_fibre", TEST_TICKS, ::shortGrassDropsPlantFibre),
        SurvivalTest("ferns_drop_plant_fibre", TEST_TICKS, ::fernsDropPlantFibre),
        SurvivalTest("shears_never_get_plant_fibre", TEST_TICKS, ::shearsNeverGetPlantFibre),
        SurvivalTest("two_sticks_make_a_hand_drill", TEST_TICKS, ::twoSticksMakeAHandDrill),
        SurvivalTest(
            "each_tinder_material_makes_one_tinder",
            TEST_TICKS,
            ::eachTinderMaterialMakesOneTinder,
        ),
        SurvivalTest("other_plants_dont_make_tinder", TEST_TICKS, ::otherPlantsDontMakeTinder),
    )
}
