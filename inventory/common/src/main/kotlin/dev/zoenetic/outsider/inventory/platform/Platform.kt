package dev.zoenetic.outsider.inventory.platform

// Mirrors survival's platform/Platform.kt. Extend with ChunkStore/PlayerStore-backed
// properties (see survival's Platform + Storage.kt) once Inventory has per-chunk or
// per-player state to persist and sync.
public interface Platform {
    public val name: String
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val register: Register
}
