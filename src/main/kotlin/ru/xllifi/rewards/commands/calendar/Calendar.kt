package ru.xllifi.rewards.commands.calendar

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.Calendar
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.ui.calendar.CalendarScreen

object CalendarCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun open(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")
    val screen = CalendarScreen(calendar, ctx.source.playerOrException)
    screen.open()

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("open") {
      calendarArgument("calendar") {
        executes { ctx -> open(ctx) }
      }
    }
  }
}

fun CommandContext<CommandSourceStack>.getCalendarArgument(calendarArgumentName: String): Calendar {
  val calendarId = StringArgumentType.getString(this, calendarArgumentName)
  val calendar = getServerAttachment().calendars.firstOrNull { it.id == calendarId }
  if (calendar == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such calendar with ID $calendarId")
  return calendar
}

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
    .map { it.id }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}

fun CommandContext<CommandSourceStack>.getCalendarAndCellArguments(
  calendarArgumentName: String,
  cellArgumentName: String
): Pair<Calendar, Calendar.Cell> {
  val calendar = this.getCalendarArgument(calendarArgumentName)
  val cell = this.getCellArgument(calendarArgumentName, cellArgumentName)
  return calendar to cell
}

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

fun CommandContext<CommandSourceStack>.getCellArgument(
  calendarArgumentName: String,
  cellArgumentName: String
): Calendar.Cell {
  val calendar = this.getCalendarArgument(calendarArgumentName)
  val cellId = StringArgumentType.getString(this, cellArgumentName)
  val cell = calendar.cells.firstOrNull { it.id == cellId }
  if (cell == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("No such cell with ID $cellId")
  return cell
}