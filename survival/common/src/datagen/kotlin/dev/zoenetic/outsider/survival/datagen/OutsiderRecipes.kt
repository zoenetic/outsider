package dev.zoenetic.outsider.survival.datagen

import dev.zoenetic.outsider.survival.registry.OutsiderLooseStones
import dev.zoenetic.outsider.survival.registry.items.OutsiderItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class OutsiderRecipes(registries: HolderLookup.Provider, output: RecipeOutput) :
    RecipeProvider(registries, output) {

    override fun buildRecipes() {
        shapeless(RecipeCategory.MISC, Items.STICK)
            .requires(OutsiderItems.FIREWOOD)
            .unlockedBy(
                "has_firewood",
                has(OutsiderItems.FIREWOOD),
            ).save(output, "outsider_survival:stick_from_firewood")

        shaped(RecipeCategory.MISC, OutsiderItems.CAMPFIRE, 1)
            .pattern("ff")
            .pattern("ff")
            .define('f', OutsiderItems.FIREWOOD)
            .unlockedBy(
                "has_firewood",
                has(OutsiderItems.FIREWOOD),

            )
            .save(output)

        shaped(RecipeCategory.BUILDING_BLOCKS, Items.COBBLESTONE, 1)
            .pattern("ss")
            .pattern("ss")
            .define('s', OutsiderLooseStones.STONE.item)
            .unlockedBy(
                "has_loose_stone",
                has(OutsiderLooseStones.STONE.item),
            )
            .save(output, "outsider_survival:cobblestone_from_loose_stone")

        shaped(RecipeCategory.MISC, OutsiderItems.TORCH, 4)
            .pattern("c")
            .pattern("s")
            .define('c', ItemTags.COALS)
            .define('s', Items.STICK)
            .unlockedBy(
                "has_stone_pickaxe",
                has(Items.STONE_PICKAXE),
            )
            .save(output, "minecraft:torch")
    }

    private class Factory(
        packOutput: PackOutput,
        registries: CompletableFuture<HolderLookup.Provider>,
    ) : Runner(packOutput, registries) {

        override fun createRecipeProvider(
            registries: HolderLookup.Provider,
            output: RecipeOutput,
        ): RecipeProvider = OutsiderRecipes(registries, output)

        override fun getName(): String = "Outsider: Survival recipes"
    }

    companion object {
        fun factory(
            output: PackOutput,
            registries: CompletableFuture<HolderLookup.Provider>,
        ): DataProvider = Factory(output, registries)
    }
}
