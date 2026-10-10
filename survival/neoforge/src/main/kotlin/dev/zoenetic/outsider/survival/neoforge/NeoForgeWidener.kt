package dev.zoenetic.outsider.survival.neoforge

import dev.zoenetic.outsider.survival.platform.Widener
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent

public object NeoForgeWidener : Widener {

    private val addValidBlockFunctions: MutableList<(BlockEntityTypeAddBlocksEvent) -> Unit> = mutableListOf()

    override fun addValidBlocks(type: BlockEntityType<*>, blocks: () -> List<Block>) {
        addValidBlockFunctions.add { event: BlockEntityTypeAddBlocksEvent ->
            event.modify(type, *blocks().toTypedArray())
        }
    }

    public fun init(bus: IEventBus) {
        bus.addListener(BlockEntityTypeAddBlocksEvent::class.java) { event ->
            addValidBlockFunctions.forEach {
                it(event)
            }
        }
    }
}
