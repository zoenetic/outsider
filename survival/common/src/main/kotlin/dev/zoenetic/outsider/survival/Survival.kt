package dev.zoenetic.outsider.survival

import dev.zoenetic.outsider.survival.conditions.PlayerConditions
import dev.zoenetic.outsider.survival.emission.EmitterIndex
import dev.zoenetic.outsider.survival.platform.Platform
import dev.zoenetic.outsider.survival.registry.*
import dev.zoenetic.outsider.survival.vitals.BreathParticles
import dev.zoenetic.outsider.survival.vitals.Exertion
import dev.zoenetic.outsider.survival.vitals.SpeedPenalty
import dev.zoenetic.outsider.survival.vitals.Vitals
import net.minecraft.server.level.ServerLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Survival {

    public const val MOD_ID: String = "outsider_survival"
    public const val NAMESPACE: String = "outsider_survival"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        ServerState.init()
        OutsiderBlocks.init()
        OutsiderComponents.init()
        OutsiderItems.init()
        OutsiderSounds.init()
        OutsiderWideners.init()
        LOGGER.info(
            "Outsider: Survival (server) starting on {} (Minecraft 26.2)",
            platform.name
        )
    }

    public fun tick(level: ServerLevel) {
        PlayerConditions.tick(level)
        Vitals.tick(level)
        Exertion.tick(level)
        SpeedPenalty.tick(level)
        BreathParticles.tick(level)
        EmitterIndex.tickDrops(level)
    }
}
