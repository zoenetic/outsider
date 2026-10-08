package dev.zoenetic.outsider.survival.gametest

import net.minecraft.gametest.framework.GameTestHelper

internal fun GameTestHelper.ensure(condition: Boolean, message: () -> String) {
    if (!condition) throw assertionException(message())
}
