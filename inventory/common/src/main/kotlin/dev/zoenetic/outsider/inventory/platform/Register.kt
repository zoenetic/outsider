package dev.zoenetic.outsider.inventory.platform

import net.minecraft.core.Holder
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import kotlin.reflect.KProperty

public operator fun <T : Any> Holder<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>
): T = this.value()

// Minimal register surface: just enough to add blocks/items. Extend to match
// survival's platform/Register.kt (blockEntity, sound, standingAndWallBlockItem, ...)
// once Inventory actually needs them - don't add unused surface ahead of that.
public interface Register {

    public fun block(
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> Block,
        propertiesFactory: () -> BlockBehaviour.Properties,
    ): Holder<Block>

    public fun blockItem(
        name: String,
        properties: Item.Properties = Item.Properties(),
        blockFactory: () -> Block,
    ): Holder<Item>
}
