package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.Permission
import net.minecraft.server.permissions.PermissionLevel
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.setServerAttachment

object AdminCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun reloadConfigs(ctx: CommandContext<CommandSourceStack>): Int {
    try {
      Main.refreshGlobalConfig()
      ctx.source.server.setServerAttachment()
      ctx.source.sendSuccess({ Component.translatable("rewards.commands.admin.reload_configs.success") }, true)
      return Command.SINGLE_SUCCESS
    } catch (e: Exception) {
      ctx.source.sendFailure(Component.translatable("rewards.commands.admin.reload_configs.failure"))
      Main.logger.error(e.stackTraceToString())
      return Command.SINGLE_FAILURE
    }
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("admin") {
      require { permissions().hasPermission(Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)) }
      literal("reload_configs") {
        executes { ctx -> reloadConfigs(ctx) }
      }
      with(AdminCalendarCommands) { register() }
      with(AdminProgressionCommands) { register() }
      literal("cosmetics") {
        with(AdminCosmeticsCommands) { register() }
      }
    }
  }
}