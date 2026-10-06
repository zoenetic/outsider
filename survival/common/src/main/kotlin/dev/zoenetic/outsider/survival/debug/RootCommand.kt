package dev.zoenetic.outsider.survival.debug

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val rootCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("outsider")
