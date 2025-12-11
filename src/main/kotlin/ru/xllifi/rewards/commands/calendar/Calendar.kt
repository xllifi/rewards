package ru.xllifi.rewards.commands.calendar

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.Calendar
import java.util.concurrent.CompletableFuture

class CalendarCommands : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("calendar")
      .then(CalendarOpenCommand().register())
}

fun CommandContext<CommandSourceStack>.getCalendarArgument(calendarArgumentName: String): Calendar {
  val calendarId = StringArgumentType.getString(this, calendarArgumentName)
  val calendar = Main.configs.calendars.firstOrNull { it.id == calendarId }
  if (calendar == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("No such calendar with ID $calendarId")
  return calendar
}

fun calendarArgument(name: String): RequiredArgumentBuilder<CommandSourceStack, String> =
  Commands
    .argument(name, StringArgumentType.string())
    .suggests(CalendarSuggestionProvider())

class CalendarSuggestionProvider : SuggestionProvider<CommandSourceStack> {
  override fun getSuggestions(
    context: CommandContext<CommandSourceStack>,
    builder: SuggestionsBuilder,
  ): CompletableFuture<Suggestions> {
    val lastInputPart = context.input.split(' ').last()
    Main.configs.calendars
      .map { it.id }
      .filter { it.startsWith(lastInputPart) }
      .forEach {
        builder.suggest(it)
      }
    return builder.buildFuture()
  }
}

fun CommandContext<CommandSourceStack>.getCalendarAndCellArguments(calendarArgumentName: String, cellArgumentName: String): Pair<Calendar, Calendar.Cell> {
  val calendar = this.getCalendarArgument(calendarArgumentName)
  val cell = this.getCellArgument(calendarArgumentName, cellArgumentName)
  return calendar to cell
}

fun CommandContext<CommandSourceStack>.getCellArgument(calendarArgumentName: String, cellArgumentName: String): Calendar.Cell {
  val calendar = this.getCalendarArgument(calendarArgumentName)
  val cellId = StringArgumentType.getString(this, cellArgumentName)
  val cell = calendar.cells.firstOrNull { it.id == cellId }
  if (cell == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("No such cell with ID $cellId")
  return cell
}

fun cellArgument(calendarArgumentName: String, cellArgumentName: String): RequiredArgumentBuilder<CommandSourceStack, String> =
  Commands
    .argument(cellArgumentName, StringArgumentType.string())
    .suggests(CellSuggestionProvider(calendarArgumentName))

class CellSuggestionProvider(
  val calendarArgumentName: String
) : SuggestionProvider<CommandSourceStack> {
  override fun getSuggestions(
    context: CommandContext<CommandSourceStack>,
    builder: SuggestionsBuilder,
  ): CompletableFuture<Suggestions> {
    val calendar = context.getCalendarArgument(calendarArgumentName)
    val lastInputPart = context.input.split(' ').last()
    calendar.cells
      .map { it.id }
      .filter { it.startsWith(lastInputPart) }
      .forEach { builder.suggest(it) }
    return builder.buildFuture()
  }
}