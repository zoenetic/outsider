package dev.zoenetic.outsider.survival.climate

import net.minecraft.world.level.chunk.LevelChunk

public object ChunkClimate {
    public fun of(chunk: LevelChunk): ClimateSample = chunk.getClimate()
}