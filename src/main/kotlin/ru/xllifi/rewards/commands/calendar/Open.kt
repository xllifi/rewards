package ru.xllifi.rewards.commands.calendar

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.serializers.text.Component

class CalendarOpenCommand : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    val calendar = context.getCalendarArgument("calendar")
    context.source.sendMessage {
      Component.text(Main.jsonSerializers.json.encodeToString(calendar))
    }

    return Command.SINGLE_SUCCESS
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("open")
      .then(
        calendarArgument("calendar").executes(this::run)
      )
}