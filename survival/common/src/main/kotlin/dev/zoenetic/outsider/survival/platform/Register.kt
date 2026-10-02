package dev.zoenetic.outsider.survival.platform

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier
import kotlin.reflect.KProperty

public operator fun <T : Any> Holder<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>
): T = this.value()

public operator fun <T : Any> Supplier<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>
): T = get()

public interface Register {

    public fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block>

    public fun blockEntity(
        name: String,
        blocksFactory: () -> Set<Block>,
        entityFactory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
    ): Holder<BlockEntityType<*>>

    public fun blockItem(
        name: String,
        blockFactory: () -> Block,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item>

    public fun <T : Any> component(
        name: String,
        builder: DataComponentType.Builder<T>,
    ): Supplier<DataComponentType<T>>

    public fun item(
        name: String,
        itemFactory: (Item.Properties) -> Item,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item>

    public fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent>

    public fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        propertiesFactory: () -> Item.Properties,
    ): Holder<Item>

}
