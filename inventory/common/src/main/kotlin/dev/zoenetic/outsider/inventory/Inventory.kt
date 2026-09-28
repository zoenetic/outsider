package dev.zoenetic.outsider.inventory

import dev.zoenetic.outsider.inventory.platform.Platform
import net.minecraft.server.level.ServerLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Inventory {

    public const val MOD_ID: String = "outsider_inventory"
    public const val NAMESPACE: String = "outsider_inventory"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        LOGGER.info(
            "Outsider: Inventory (server) starting on {} (Minecraft 26.2)",
            platform.name
        )
    }

    public fun tick(level: ServerLevel) {
        // TODO: nothing to tick yet.
    }
}
