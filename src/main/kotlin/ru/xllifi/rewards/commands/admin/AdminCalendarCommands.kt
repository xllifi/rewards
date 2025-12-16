package ru.xllifi.rewards.commands.admin

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.commands.calendarArgument
import ru.xllifi.rewards.commands.cellArgument
import ru.xllifi.rewards.commands.getCalendarAndCellArguments
import net.minecraft.network.chat.Component
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.getCalendarArgument
import ru.xllifi.rewards.sql.CollectedCell
import ru.xllifi.rewards.sql.CollectedCellTable
import ru.xllifi.rewards.sql.setCellCollectedFor

object AdminCalendarCommands : Command {
  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("calendar") {
      with(AdminCellsCommands) { register() }
    }
  }
}

object AdminCellsCommands : Command {
  fun uncollectAllForEveryone(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")

    transaction(Main.database) {
      val cells = CollectedCell.find {
        CollectedCellTable.calendarId eq calendar.id
      }
      for (cell in cells) {
        cell.delete()
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.calendar.uncollect_all_for_everyone.success",
        calendar.id
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun collectAll(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")
    val player = EntityArgument.getPlayer(ctx, "player")

    transaction(Main.database) {
      for (cell in calendar.cells) {
        CollectedCell.new {
          playerUuid = player.uuid
          calendarId = calendar.id
          cellId = cell.id
        }
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.calendar.collect_all.success",
        calendar.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun uncollectAll(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")
    val player = EntityArgument.getPlayer(ctx, "player")

    transaction(Main.database) {
      val cells = CollectedCell.find {
        CollectedCellTable.playerUuid eq player.uuid
        CollectedCellTable.calendarId eq calendar.id
      }
      for (cell in cells) {
        cell.delete()
      }
    }

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.calendar.uncollect_all.success",
        calendar.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun collect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    calendar.setCellCollectedFor(player, cell, true)

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.calendar.cell.collect.success",
        cell.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  fun uncollect(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    calendar.setCellCollectedFor(player, cell, false)

    ctx.source.sendSuccess({
      Component.translatable(
        "rewards.commands.admin.calendar.cell.uncollect.success",
        cell.id, player.plainTextName
      )
    }, true)
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("cell") {
      literal("uncollect_all_for_everyone") {
        calendarArgument("calendar") {
          executes { ctx -> uncollectAllForEveryone(ctx) }
        }
      }
      argument("player", EntityArgument.player()) {
        calendarArgument("calendar") {
          literal("collect_all") { executes { ctx -> collectAll(ctx) } }
          literal("uncollect_all") { executes { ctx -> uncollectAll(ctx) } }
          cellArgument("calendar", "cell") {
            literal("collect") { executes { ctx -> collect(ctx) } }
            literal("uncollect") { executes { ctx -> uncollect(ctx) } }
          }
        }
      }
    }
  }
}