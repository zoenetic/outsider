package dev.zoenetic.outsider.survival.fabric

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.platform.Register
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.StandingAndWallBlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

public object FabricRegister : Register {

    override fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block> {
        val key = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        return Registry.registerForHolder(
            BuiltInRegistries.BLOCK,
            key,
            blockFactory(propertiesFactory().setId(key))
        )
    }

    override fun blockEntity(
        name: String,
        entityFactory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
        blocksFactory: () -> Set<Block>
    ): Holder<BlockEntityType<*>> {
        val key = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        lateinit var type: BlockEntityType<*>
        type = BlockEntityType({ pos, state -> entityFactory(type, pos, state) }, blocksFactory())
        return Registry.registerForHolder(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type)
    }

    override fun blockItem(
        name: String,
        properties: Item.Properties,
        blockFactory: () -> Block
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        val holder = Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            BlockItem(blockFactory(), properties.setId(key))
        )
        return holder.value().builtInRegistryHolder()
    }

    override fun <T : Any> component(
        name: String,
        builder: DataComponentType.Builder<T>
    ): Supplier<DataComponentType<T>> {
        val key = ResourceKey.create(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        val type = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            key,
            builder.build(),
        )
        return Supplier { type }
    }

    override fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent> {
        val id = Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        val holder = Registry.registerForHolder(
            BuiltInRegistries.SOUND_EVENT,
            ResourceKey.create(Registries.SOUND_EVENT, id),
            factory(id)
        )
        return holder
    }

    override fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        properties: Item.Properties
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        val holder = Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            StandingAndWallBlockItem(
                block(),
                wallBlock(),
                attachmentDirection,
                properties.setId(key)
            )
        )
        return holder.value().builtInRegistryHolder()
    }
}
