package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.xllifi.rewards.commands.Command

object AdminCommands : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("admin")
      .requires { it.hasPermission(2) }
      .then(AdminCalendarCommands.register())
}