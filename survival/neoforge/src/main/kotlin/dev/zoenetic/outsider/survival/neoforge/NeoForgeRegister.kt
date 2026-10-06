package dev.zoenetic.outsider.survival.neoforge

import com.mojang.serialization.MapCodec
import dev.zoenetic.outsider.survival.Survival.NAMESPACE
import dev.zoenetic.outsider.survival.platform.Register
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.StandingAndWallBlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.functions.LootItemFunction
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Function
import java.util.function.Supplier

public object NeoForgeRegister : Register {

    private val attachments: DeferredRegister<AttachmentType<*>> = DeferredRegister.create(
        NeoForgeRegistries.ATTACHMENT_TYPES,
        NAMESPACE,
    )
    private val blocks: DeferredRegister.Blocks = DeferredRegister.createBlocks(NAMESPACE)
    private val blockEntities: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NAMESPACE)
    private val components: DeferredRegister<DataComponentType<*>> =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NAMESPACE)
    private val items: DeferredRegister.Items = DeferredRegister.createItems(NAMESPACE)
    private val lootFunctions: DeferredRegister<MapCodec<out LootItemFunction>> =
        DeferredRegister.create(
            Registries.LOOT_FUNCTION_TYPE,
            NAMESPACE,
        )
    private val sounds: DeferredRegister<SoundEvent> = DeferredRegister.create(
        BuiltInRegistries.SOUND_EVENT,
        NAMESPACE,
    )

    public fun <T : Any> attachment(
        name: String,
        type: () -> AttachmentType<T>,
    ): DeferredHolder<AttachmentType<*>, AttachmentType<T>> =
        attachments.register(name, Supplier(type))

    override fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): DeferredBlock<Block> {
        val holder = blocks.registerBlock(name, blockFactory, Supplier { propertiesFactory() })
        return holder
    }

    override fun blockEntity(
        name: String,
        blocksFactory: () -> Set<Block>,
        entityFactory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
    ): Holder<BlockEntityType<*>> {
        lateinit var holder: DeferredHolder<BlockEntityType<*>, BlockEntityType<*>>
        holder = blockEntities.register(
            name,
            Supplier {
                BlockEntityType(
                    { pos, state -> entityFactory(holder.value(), pos, state) },
                    blocksFactory(),
                )
            },
        )
        return holder
    }

    override fun blockItem(
        name: String,
        blockFactory: () -> Block,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = items.registerItem(
        name,
        { props ->
            BlockItem(blockFactory(), props)
        },
        Supplier { propertiesFactory() },
    )

    override fun <T : Any> component(
        name: String,
        builder: DataComponentType.Builder<T>,
    ): DeferredHolder<DataComponentType<*>, DataComponentType<T>> =
        components.register(name, Supplier { builder.build() })

    override fun item(
        name: String,
        itemFactory: (Item.Properties) -> Item,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = items.registerItem(
        name,
        { props ->
            itemFactory(props)
        },
        Supplier { propertiesFactory() },
    )

    override fun lootFunction(name: String, codec: MapCodec<out LootItemFunction>) {
        lootFunctions.register(name, Supplier { codec })
    }

    override fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent,
    ): DeferredHolder<SoundEvent, SoundEvent> =
        sounds.register(name, Function { id: Identifier -> factory(id) })

    override fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item> = items.registerItem(
        name,
        { props ->
            StandingAndWallBlockItem(block(), wallBlock(), attachmentDirection, props)
        },
        Supplier { propertiesFactory() },
    )

    public fun init(bus: IEventBus) {
        attachments.register(bus)
        blocks.register(bus)
        blockEntities.register(bus)
        components.register(bus)
        items.register(bus)
        lootFunctions.register(bus)
        sounds.register(bus)
    }
}
