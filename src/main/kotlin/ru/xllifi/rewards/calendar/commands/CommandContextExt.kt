package ru.xllifi.rewards.calendar.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.calendar.Calendar
import ru.xllifi.rewards.config.getServerAttachment

fun CommandContext<CommandSourceStack>.getCalendarArgument(calendarArgumentName: String): Calendar {
  val calendarId = StringArgumentType.getString(this, calendarArgumentName)
  val calendar = getServerAttachment().calendars.firstOrNull { it.id == calendarId }
  if (calendar == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such calendar with ID $calendarId")
  return calendar
}

fun CommandContext<CommandSourceStack>.getCalendarAndCellArguments(
  calendarArgumentName: String,
  cellArgumentName: String
): Pair<Calendar, Calendar.Cell> {
  val calendar = this.getCalendarArgument(calendarArgumentName)
  val cell = this.getCellArgument(calendarArgumentName, cellArgumentName)
  return calendar to cell
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