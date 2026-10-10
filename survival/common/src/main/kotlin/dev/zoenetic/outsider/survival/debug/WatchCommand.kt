package dev.zoenetic.outsider.survival.debug

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

public val watchCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("watch")
        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
        .executes { context ->
            val source = context.source
            val player = source.playerOrException
            if (!WatcherRegistry.registry.contains(player.uuid)) {
                WatcherRegistry.add(player.uuid)
                source.sendSuccess({
                    Component.literal("Added watcher")
                }, false)
            } else {
                WatcherRegistry.remove(player.uuid)
                source.sendSuccess({
                    Component.literal("Removed watcher")
                }, false)
            }
            return@executes 1
        }
