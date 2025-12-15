package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.setServerAttachment
import ru.xllifi.rewards.loadMainConfig
import ru.xllifi.rewards.logger

object AdminCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun reloadConfigs(ctx: CommandContext<CommandSourceStack>): Int {
    try {
      Main.globalConfig = loadMainConfig()
      ctx.source.server.setServerAttachment()
      ctx.source.sendSuccess({ Component.translatable("rewards.commands.admin.reload_configs.success") }, true)
      return Command.SINGLE_SUCCESS
    } catch (e: Exception) {
      ctx.source.sendFailure(Component.translatable("rewards.commands.admin.reload_configs.failure"))
      logger.error(e.stackTraceToString())
      return Command.SINGLE_FAILURE
    }
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("admin") {
      require { hasPermission(2) }
      literal("reload_configs") {
        executes { ctx -> reloadConfigs(ctx) }
      }
      with(AdminCalendarCommands) { register() }
    }
  }
}