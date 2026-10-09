package dev.zoenetic.outsider.survival.registry.blocks

import net.minecraft.world.level.block.state.properties.IntegerProperty

public object OutsiderBlockStateProperties {
    @JvmField
    public val FUEL_LEVEL: IntegerProperty = IntegerProperty.create("fuel_level", 0, 15)
}
