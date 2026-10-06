package dev.zoenetic.outsider.survival.datagen

import dev.zoenetic.outsider.survival.registry.OutsiderItems
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
            ).save(output)

        shaped(RecipeCategory.MISC, OutsiderItems.CAMPFIRE, 1)
            .pattern("ff")
            .pattern("ff")
            .define('f', OutsiderItems.FIREWOOD)
            .unlockedBy(
                "has_firewood",
                has(OutsiderItems.FIREWOOD),

            )
            .save(output)

        shaped(RecipeCategory.MISC, OutsiderItems.TORCH, 1)
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
