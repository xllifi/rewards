package ru.xllifi.rewards.calendar.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import ru.xllifi.rewards.calendar.ui.CalendarScreen
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.getServerAttachment

object CalendarCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun open(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")
    if (!calendar.isActive) {
      ctx.source.sendFailure(
        Component.translatable("rewards.commands.calendar.open.failure.inactive", ctx.getServerAttachment().audiences.asNative(calendar.title))
      )
      return Command.SINGLE_FAILURE
    }
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
