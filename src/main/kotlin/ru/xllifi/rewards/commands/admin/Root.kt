package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.xllifi.rewards.commands.Command

object AdminCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("admin") {
      require { hasPermission(2) }
      with (AdminCalendarCommands) { register() }
    }
  }
}