package ru.xllifi.rewards.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import ru.xllifi.rewards.commands.calendar.calendarArgument
import ru.xllifi.rewards.commands.calendar.cellArgument
import ru.xllifi.rewards.commands.calendar.getCalendarAndCellArguments
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.sql.isCellCollectedBy

object Debug {
  var calendarsIgnoreCellStatus: Boolean = false
}

object DebugCommands : Command {
  fun showServer(ctx: CommandContext<CommandSourceStack>): Int {
    val serverAttachment = ctx.getServerAttachment()
    ctx.source.sendSystemMessage(
      Component.literal(serverAttachment.toString())
    )
    return Command.SINGLE_SUCCESS
  }

  fun hasCollectedCell(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")
    ctx.source.sendSystemMessage(
      Component.literal(
        "Cell ${cell.id} in ${calendar.id} collection status: ${
          calendar.isCellCollectedBy(player, cell)
        }"
      )
    )

    return Command.SINGLE_SUCCESS
  }

  fun showCellStatus(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    ctx.source.sendMessage {
      MiniMessage.miniMessage().deserialize(
        "<gray>Cell <yellow>${cell.id}</yellow> in <yellow>${calendar.id}</yellow> status: ${calendar.getCellStatus(cell)}"
      )
    }

    return Command.SINGLE_SUCCESS
  }

  fun showCellRewards(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")

    val rewards = cell.rewards.map {
      Component.empty().append(it.lore()).append("\n")
    }.reduce { acc, com -> acc.append(com) }
    ctx.source.sendSystemMessage(rewards)

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("debug") {
      require { hasPermission(2) }
      require { FabricLoader.getInstance().isDevelopmentEnvironment }
      literal("show_server_attachment") {
        executes { showServer(it) }
      }
      literal("toggle_calendarsIgnoreCellStatus") {
        executes {
          Debug.calendarsIgnoreCellStatus = !Debug.calendarsIgnoreCellStatus
          Command.SINGLE_SUCCESS
        }
      }
      literal("check_cell") {
        calendarArgument("calendar") {
          cellArgument("calendar", "cell") {
            literal("status") {
              executes { showCellStatus(it) }
            }
            literal("rewards") {
              executes { showCellRewards(it) }
            }
            literal("is_collected_by") {
              argument("player", EntityArgument.player()) {
                executes { hasCollectedCell(it) }
              }
            }
          }
        }
      }
    }
  }
}