package ru.xllifi.rewards.commands.calendar

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.serializers.text.Component

object CalendarOpenCommand : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    val calendar = ctx.getCalendarArgument("calendar")
    ctx.source.sendMessage {
      Component.text(ctx.getServerAttachment().jsonSerializers.json.encodeToString(calendar))
    }

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("open") {
      calendarArgument("calendar") {
        executes { ctx -> run(ctx) }
      }
    }
  }
}