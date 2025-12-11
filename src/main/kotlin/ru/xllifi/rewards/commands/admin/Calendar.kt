package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.commands.calendar.calendarArgument
import ru.xllifi.rewards.commands.calendar.cellArgument
import ru.xllifi.rewards.commands.calendar.getCalendarAndCellArguments
import ru.xllifi.rewards.config.collectCell
import ru.xllifi.rewards.config.uncollectCell
import ru.xllifi.rewards.serializers.text.Component

object AdminCalendarCommands : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("calendar")
      .then(AdminCellsCommands.register())
}

object AdminCellsCommands : Command {
  override fun run(context: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun collect(context: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = context.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(context, "player")

    val playerData = player.collectCell(calendar, cell)
    return if (playerData != null) {
      context.source.sendSuccess({ Main.audiences.asNative(Component.text("Cell ${cell.id} is now collected: ${playerData.collectedCalendarCells[calendar.id]}")) }, false)
      Command.SINGLE_SUCCESS
    } else {
      context.source.sendFailure(Main.audiences.asNative(Component.text("Failed to find ${player.plainTextName}'s (${player.uuid}) entry")))
      Command.SINGLE_FAILURE
    }
  }

  fun uncollect(context: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = context.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(context, "player")

    val playerData = player.uncollectCell(calendar, cell)
    return if (playerData != null) {
      context.source.sendSuccess({ Main.audiences.asNative(Component.text("Cell ${cell.id} is no more collected: ${playerData.collectedCalendarCells[calendar.id]}")) }, false)
      Command.SINGLE_SUCCESS
    } else {
      context.source.sendFailure(Main.audiences.asNative(Component.text("Failed to find ${player.plainTextName}'s (${player.uuid}) entry")))
      Command.SINGLE_FAILURE
    }
  }

  override fun register(): LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("cell").then(
      Commands.argument("player", EntityArgument.player()).then(
        calendarArgument("calendar").then(
          cellArgument("calendar", "cell").then(
            Commands.literal("collect").executes(this::collect)
          ).then(
            Commands.literal("uncollect").executes(this::uncollect)
          )
        )
      )
    )
}