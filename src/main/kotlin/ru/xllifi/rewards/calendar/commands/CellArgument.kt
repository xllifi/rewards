package ru.xllifi.rewards.calendar.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.calendar.Calendar

fun DSLCommandNode<CommandSourceStack>.cellArgument(
  calendarArgumentName: String,
  cellArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(cellArgumentName, StringArgumentType.string()) {
    suggest { cellSuggestions(calendarArgumentName, it) }
    setup()
  }

fun SuggestionsBuilder.cellSuggestions(calendarArgumentName: String, ctx: CommandContext<CommandSourceStack>) {
  val calendar = ctx.getCalendarArgument(calendarArgumentName)
  val lastInputPart = ctx.input.split(' ').last()
  calendar.cells
    .map { it.id }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}
