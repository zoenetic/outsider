package dev.zoenetic.outsider.inventory.fabric

import dev.zoenetic.outsider.inventory.Inventory
import dev.zoenetic.outsider.inventory.platform.Register
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

public object FabricRegister : Register {

    override fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block> {
        val key = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Inventory.NAMESPACE, name)
        )
        return Registry.registerForHolder(
            BuiltInRegistries.BLOCK,
            key,
            blockFactory(propertiesFactory().setId(key))
        )
    }

    override fun blockItem(
        name: String,
        properties: Item.Properties,
        blockFactory: () -> Block
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Inventory.NAMESPACE, name)
        )
        val holder = Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            BlockItem(blockFactory(), properties.setId(key))
        )
        return holder.value().builtInRegistryHolder()
    }
}
