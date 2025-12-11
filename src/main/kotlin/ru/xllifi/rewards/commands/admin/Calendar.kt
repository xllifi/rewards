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
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.config.uncollectCell
import ru.xllifi.rewards.serializers.text.Component

object AdminCalendarCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("calendar") {
      with(AdminCellsCommands) { register() }
    }
  }
//    Commands.literal("calendar")
//      .then(AdminCellsCommands.register())
}

object AdminCellsCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun collect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    val playerData = player.collectCell(calendar, cell)
    return if (playerData != null) {
      ctx.source.sendSuccess({ ctx.getServerAttachment().audiences.asNative(Component.text("Cell ${cell.id} is now collected: ${playerData.collectedCalendarCells[calendar.id]}")) }, false)
      Command.SINGLE_SUCCESS
    } else {
      ctx.source.sendFailure(ctx.getServerAttachment().audiences.asNative(Component.text("Failed to find ${player.plainTextName}'s (${player.uuid}) entry")))
      Command.SINGLE_FAILURE
    }
  }

  fun uncollect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    val playerData = player.uncollectCell(calendar, cell)
    return if (playerData != null) {
      ctx.source.sendSuccess({ ctx.getServerAttachment().audiences.asNative(Component.text("Cell ${cell.id} is no more collected: ${playerData.collectedCalendarCells[calendar.id]}")) }, false)
      Command.SINGLE_SUCCESS
    } else {
      ctx.source.sendFailure(ctx.getServerAttachment().audiences.asNative(Component.text("Failed to find ${player.plainTextName}'s (${player.uuid}) entry")))
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