package ru.xllifi.rewards.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.xllifi.rewards.commands.admin.AdminCommands
import ru.xllifi.rewards.commands.calendar.CalendarCommands
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.utils.plus

class RewardsCommands : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    context.source.sendMessage {
      Component
        .text("Running rewards version ")
        .color(NamedTextColor.GRAY) +
        Component
          .text(FabricLoader.getInstance().getModContainer(modId).get().metadata.version.friendlyString)
          .color(NamedTextColor.YELLOW)
    }
    return Command.SINGLE_SUCCESS
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("rewards")
      .executes(this::run)
      .then(CalendarCommands().register())
      .then(AdminCommands.register())
}

interface Command : com.mojang.brigadier.Command<CommandSourceStack> {
  override fun run(context: CommandContext<CommandSourceStack>): Int
  fun register(): LiteralArgumentBuilder<CommandSourceStack>

  companion object {
    const val SINGLE_SUCCESS = 1
    const val SINGLE_FAILURE = 0
  }
}