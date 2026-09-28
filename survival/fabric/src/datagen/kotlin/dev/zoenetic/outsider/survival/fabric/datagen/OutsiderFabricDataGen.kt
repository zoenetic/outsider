package dev.zoenetic.outsider.survival.fabric.datagen

import dev.zoenetic.outsider.survival.datagen.SurvivalDataGen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object OutsiderFabricDataGen : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val pack = generator.createPack()
        for (factory in SurvivalDataGen.providers) {
            pack.addProvider { output, registries -> factory(output, registries) }
        }
        pack.addProvider(::OutsiderFabricModelProvider)
    }
}
