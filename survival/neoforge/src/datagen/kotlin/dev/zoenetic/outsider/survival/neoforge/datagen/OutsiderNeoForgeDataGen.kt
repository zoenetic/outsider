package dev.zoenetic.outsider.survival.neoforge.datagen

import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.datagen.SurvivalDataGen
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.data.event.GatherDataEvent

@Mod(Survival.MOD_ID)
class OutsiderNeoForgeDataGen(modBus: IEventBus) {

    init {
        modBus.addListener(GatherDataEvent.Client::class.java) { event ->
            for (factory in SurvivalDataGen.providers) {
                event.createProvider { output, registries -> factory(output, registries) }
            }
            event.createProvider { output -> OutsiderNeoForgeModelProvider(output, Survival.NAMESPACE) }
        }
    }
}