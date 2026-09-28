package dev.zoenetic.outsider.inventory.neoforge

import dev.zoenetic.outsider.inventory.Inventory.NAMESPACE
import dev.zoenetic.outsider.inventory.platform.Register
import net.minecraft.core.Holder
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

public object NeoForgeRegister : Register {

    private val blocks: DeferredRegister.Blocks = DeferredRegister.createBlocks(NAMESPACE)
    private val items: DeferredRegister.Items = DeferredRegister.createItems(NAMESPACE)

    override fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block> =
        blocks.registerBlock(name, blockFactory, Supplier { propertiesFactory() })

    override fun blockItem(
        name: String,
        properties: Item.Properties,
        blockFactory: () -> Block
    ): Holder<Item> =
        items.registerItem(name, { props -> BlockItem(blockFactory(), props) }, Supplier { properties })

    public fun init(bus: IEventBus) {
        blocks.register(bus)
        items.register(bus)
    }
}
