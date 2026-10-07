package dev.zoenetic.outsider.survival.gametest

import dev.zoenetic.outsider.survival.fuel.Fuel
import dev.zoenetic.outsider.survival.registry.OutsiderComponents
import dev.zoenetic.outsider.survival.registry.OutsiderItems
import dev.zoenetic.outsider.survival.superstack.asSuperStackOrNull
import dev.zoenetic.outsider.survival.superstack.moveIntoSuperStack
import net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.util.Unit
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.component.BundleContents

internal fun torches(count: Int, fuel: Int = Fuel.MAX.level, lit: Boolean = false): ItemStack =
    ItemStack(OutsiderItems.TORCH, count).apply {
        val _ = set(OutsiderComponents.FUEL_LEVEL, Fuel(fuel))
        if (lit) {
            val _ = set(OutsiderComponents.LIT, Unit.INSTANCE)
        }
    }

internal fun GameTestHelper.superStackOf(plain: ItemStack): ItemStack = plain.moveIntoSuperStack()
    ?: throw assertionException("expected $plain to wrap into a super stack")

/** A super stack whose active torch is lit (lighting goes through the write routing). */
internal fun GameTestHelper.litSuperStackOf(plain: ItemStack): ItemStack =
    superStackOf(plain).apply { val _ = set(OutsiderComponents.LIT, Unit.INSTANCE) }

/** [full] full torches plus [burnt] part-burnt ones; the part-burnt ones are active. */
internal fun GameTestHelper.mixedSuperStackOf(full: Int, burnt: Int, burntFuel: Int): ItemStack =
    superStackOf(torches(full)).apply {
        val _ = asSuperStackOrNull()?.insert(torches(burnt, fuel = burntFuel))
    }

internal fun ItemStack.groups(): List<ItemStackTemplate> = getOrDefault(
    BUNDLE_CONTENTS,
    BundleContents.EMPTY,
).items()

internal fun ItemStack.litCount(): Int =
    groups().filter { it.get(OutsiderComponents.LIT) != null }.sumOf { it.count }

internal fun ItemStack.countWithFuel(fuel: Int): Int =
    groups().filter { it.get(OutsiderComponents.FUEL_LEVEL)?.level == fuel }.sumOf { it.count }

internal fun ItemStack.fuelLevel(): Int =
    getOrDefault(OutsiderComponents.FUEL_LEVEL, Fuel.MAX).level

internal fun GameTestHelper.totalIn(stack: ItemStack): Int = stack.asSuperStackOrNull()?.count
    ?: throw assertionException("expected a super stack, found $stack")

internal fun GameTestHelper.ensure(condition: Boolean, message: () -> String) {
    if (!condition) throw assertionException(message())
}
