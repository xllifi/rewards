package ru.xllifi.rewards.calendar.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.config.getServerAttachment

fun DSLCommandNode<CommandSourceStack>.calendarArgument(
  calendarArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(calendarArgumentName, StringArgumentType.string()) {
    suggest { calendarSuggestions(it) }
    setup()
  }

fun SuggestionsBuilder.calendarSuggestions(ctx: CommandContext<CommandSourceStack>) {
  val lastInputPart = ctx.input.split(' ').last()
  ctx.getServerAttachment().calendars
    .filter { it.isActive }
    .map { it.id }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}