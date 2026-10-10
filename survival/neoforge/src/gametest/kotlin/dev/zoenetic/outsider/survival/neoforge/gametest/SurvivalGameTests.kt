package dev.zoenetic.outsider.survival.neoforge.gametest

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.outsider.survival.Survival
import dev.zoenetic.outsider.survival.gametest.SurvivalTest
import dev.zoenetic.outsider.survival.gametest.SurvivalTests
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.GameTestInstance
import net.minecraft.gametest.framework.TestData
import net.minecraft.gametest.framework.TestEnvironmentDefinition
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Rotation
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.RegisterGameTestsEvent
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

// Not an @EventBusSubscriber: KotlinLangForge injects those once per @Mod class, and this
// mod has several (main, client, datagen), so the tests would be registered more than once.
@Mod(Survival.MOD_ID)
public class SurvivalGameTests(modBus: IEventBus) {

    init {
        TEST_INSTANCE_TYPES.register(modBus)
        modBus.addListener(RegisterGameTestsEvent::class.java, ::register)
    }

    private val defaultEnvironment: Holder<TestEnvironmentDefinition<*>> =
        Holder.direct(
            TestEnvironmentDefinition.AllOf(emptyList()),
        )

    private val emptyStructure: Identifier = Identifier.withDefaultNamespace("empty")

    private fun register(event: RegisterGameTestsEvent) {
        for (test in SurvivalTests.ALL) {
            event.registerTest(
                Identifier.fromNamespaceAndPath(Survival.NAMESPACE, test.name),
                SharedGameTest(
                    test,
                    TestData(
                        defaultEnvironment,
                        emptyStructure,
                        test.maxTicks,
                        0,
                        true,
                        Rotation.NONE,
                    ),
                ),
            )
        }
    }

    private companion object {
        // Test instances are synced to clients as part of the `test_instance` registry, which
        // serialises each one through its type's codec — so that codec must be registered.
        val TEST_INSTANCE_TYPES: DeferredRegister<MapCodec<out GameTestInstance>> =
            DeferredRegister.create(Registries.TEST_INSTANCE_TYPE, Survival.NAMESPACE)

        init {
            TEST_INSTANCE_TYPES.register("shared", Supplier { SharedGameTest.CODEC })
        }
    }
}

private class SharedGameTest(
    private val test: SurvivalTest,
    data: TestData<Holder<TestEnvironmentDefinition<*>>>,
) : GameTestInstance(data) {

    override fun run(helper: GameTestHelper) {
        test.run(helper)
    }

    override fun codec(): MapCodec<out GameTestInstance> = CODEC

    override fun typeDescription(): MutableComponent = Component.literal(test.name)

    companion object {
        private val TEST_CODEC: Codec<SurvivalTest> = Codec.STRING.comapFlatMap(
            { name ->
                SurvivalTests.ALL.firstOrNull { it.name == name }
                    ?.let { DataResult.success(it) }
                    ?: DataResult.error { "Unknown survival test: $name" }
            },
            SurvivalTest::name,
        )

        val CODEC: MapCodec<SharedGameTest> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                TEST_CODEC.fieldOf("test").forGetter(SharedGameTest::test),
                TestData.CODEC.forGetter(SharedGameTest::info),
            ).apply(i, ::SharedGameTest)
        }
    }
}
