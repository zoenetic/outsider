package dev.zoenetic.unbidden.fx

import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Fx {

    public const val MOD_ID: String = "unbidden_fx"
    public const val NAMESPACE: String = "unbidden"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public fun init(platformName: String) {
        LOGGER.info("Unbidden: FX starting on {} (Minecraft 26.2)", platformName)
    }

    public fun initClient(platformName: String) {
        LOGGER.info("Unbidden: FX client starting on {}", platformName)
    }
}
