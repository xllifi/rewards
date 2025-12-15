package ru.xllifi.rewards.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import ru.xllifi.rewards.commands.calendar.calendarArgument
import ru.xllifi.rewards.commands.calendar.cellArgument
import ru.xllifi.rewards.commands.calendar.getCalendarAndCellArguments
import ru.xllifi.rewards.config.getServerAttachment
//import ru.xllifi.rewards.serializers.text.Component


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
      Component.literal("Cell ${cell.id} in ${calendar.id} collection status: ${calendar.isCellCollected(player, cell)}")
    )

    return Command.SINGLE_SUCCESS
  }

  fun isCellUnlocked(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")
    ctx.source.sendMessage {
      MiniMessage.miniMessage().deserialize(
        "<gray>Cell <yellow>${cell.id}</yellow> in <yellow>${calendar.id}</yellow> unlocking status: ${cell.unlockCondition.status(player)}"
      )
    }

    return Command.SINGLE_SUCCESS
  }

  fun showCellConditions(ctx: CommandContext<CommandSourceStack>): Int {
    val (calendar, cell) = ctx.getCalendarAndCellArguments("calendar", "cell")
    val player = EntityArgument.getPlayer(ctx, "player")

    val mutableComponent = Component.empty()
    cell.unlockCondition.lore(player).forEach {
      mutableComponent.append(it).append("\n")
    }
    ctx.source.sendSystemMessage(mutableComponent)

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("debug") {
      require { hasPermission(2) }
      require { FabricLoader.getInstance().isDevelopmentEnvironment }
      literal("show_server_attachment") {
        executes { showServer(it) }
      }
      literal("check_cell") {
        calendarArgument("calendar") {
          cellArgument("calendar", "cell") {
            argument("player", EntityArgument.player()) {
              literal("collected") {
                executes { hasCollectedCell(it) }
              }
              literal("unlocked") {
                executes { isCellUnlocked(it) }
              }
              literal("conditions") {
                executes { showCellConditions(it) }
              }
            }
          }
        }
      }
    }
  }
}