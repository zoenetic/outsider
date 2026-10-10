package dev.zoenetic.outsider.survival.datagen.models

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.zoenetic.outsider.survival.stone.loose.LooseStone
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneArrangement.Box
import dev.zoenetic.outsider.survival.stone.loose.LooseStoneBlock
import dev.zoenetic.outsider.survival.stone.loose.MAX_STONES
import net.minecraft.client.data.models.ItemModelOutput
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.blockstates.PropertyDispatch
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelInstance
import net.minecraft.client.data.models.model.ModelLocationUtils
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.renderer.block.dispatch.Variant
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED
import java.util.function.BiConsumer
import java.util.function.Consumer

private const val STONE_SLOT = "stone"

private fun modelForArrangement(set: Set<Box>, stone: Identifier): ModelInstance = ModelInstance {
    JsonObject().apply {
        addProperty("parent", "minecraft:block/block")
        add(
            "textures",
            JsonObject().apply {
                addProperty("particle", stone.toString())
                addProperty(STONE_SLOT, stone.toString())
            },
        )
        add("elements", JsonArray().apply { set.forEach { add(element(it)) } })
    }
}

fun looseStoneModel(
    looseStone: LooseStone,
    blockStateOutput: Consumer<BlockModelDefinitionGenerator>,
    modelOutput: BiConsumer<Identifier, ModelInstance>,
    itemModelOutput: ItemModelOutput,
) {
    val block = looseStone.block
    val item = looseStone.item

    val modelsByCount: Map<Int, List<Identifier>> =
        (1..MAX_STONES).associateWith { count ->
            val arrangements = looseStone.arrangement.forCount(count)
            val models = arrangements.distinct().withIndex().associate { (index, arrangement) ->
                val id = ModelLocationUtils.getModelLocation(block, "_${count}_$index")
                modelOutput.accept(id, modelForArrangement(arrangement, looseStone.texture))
                arrangement to id
            }
            arrangements.map(models::getValue)
        }
    blockStateOutput.accept(
        MultiVariantGenerator.dispatch(block).with(
            PropertyDispatch.initial(LooseStoneBlock.STONES, WATERLOGGED).generate { stones, _ ->
                variants(modelsByCount.getValue(stones).map { Variant(it) })
            },
        ),
    )
    val itemModel = ModelTemplates.FLAT_ITEM.create(item, TextureMapping.layer0(item), modelOutput)
    itemModelOutput.accept(item, ItemModelUtils.plainModel(itemModel))
}

private fun element(box: Box): JsonObject = JsonObject().apply {
    add("from", ints(box.minX, 0, box.minZ))
    add("to", ints(box.maxX, box.height, box.maxZ))
    add(
        "faces",
        JsonObject().apply {
            for (direction in Direction.entries) {
                add(
                    direction.serializedName,
                    JsonObject().apply {
                        addProperty("texture", "#$STONE_SLOT")
                        if (direction == Direction.DOWN) addProperty("cullface", "down")
                    },
                )
            }
        },
    )
}

private fun ints(vararg values: Int): JsonArray = JsonArray().apply { values.forEach(::add) }
