package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.commands.calendar.calendarArgument
import ru.xllifi.rewards.commands.calendar.cellArgument
import ru.xllifi.rewards.commands.calendar.getCalendarAndCellArguments
import ru.xllifi.rewards.config.collectCell
import ru.xllifi.rewards.config.uncollectCell
import net.minecraft.network.chat.Component

object AdminCalendarCommands : Command {
  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("calendar") {
      with(AdminCellsCommands) { register() }
    }
  }
}

object AdminCellsCommands : Command {
  fun collect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    val playerData = player.collectCell(calendar, cell)
    return if (playerData != null) {
      ctx.source.sendSuccess({
        Component.translatable(
          "rewards.commands.admin.calendar.cell.collect.success",
          cell.id, player.plainTextName
        )
      }, true)
      Command.SINGLE_SUCCESS
    } else {
      ctx.source.sendFailure(
        Component.translatable(
          "rewards.commands.admin.calendar.cell.generic.failure.no_player",
          player.plainTextName, player.stringUUID,
        )
      )
      Command.SINGLE_FAILURE
    }
  }

  fun uncollect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    val playerData = player.uncollectCell(calendar, cell)
    return if (playerData != null) {
      ctx.source.sendSuccess({
        Component.translatable(
          "rewards.commands.admin.calendar.cell.uncollect.success",
          cell.id, player.plainTextName
        )
      }, true)
      Command.SINGLE_SUCCESS
    } else {
      ctx.source.sendFailure(
        Component.translatable(
          "rewards.commands.admin.calendar.cell.generic.failure.no_player",
          player.plainTextName, player.stringUUID,
        )
      )
      Command.SINGLE_FAILURE
    }
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("cell") {
      argument("player", EntityArgument.player()) {
        calendarArgument("calendar") {
          cellArgument("calendar", "cell") {
            literal("collect") { executes { ctx -> collect(ctx) } }
            literal("uncollect") { executes { ctx -> uncollect(ctx) } }
          }
        }
      }
    }
  }
}