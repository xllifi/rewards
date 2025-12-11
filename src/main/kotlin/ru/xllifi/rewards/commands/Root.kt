package ru.xllifi.rewards.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.commands.admin.AdminCommands
import ru.xllifi.rewards.commands.calendar.CalendarCommands
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.utils.plus

object RewardsCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    ctx.source.sendMessage {
      Component
        .text("Running rewards version ")
        .color(NamedTextColor.GRAY) +
        Component
          .text(FabricLoader.getInstance().getModContainer(modId).get().metadata.version.friendlyString)
          .color(NamedTextColor.YELLOW)
    }
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    with(CalendarCommands) { register() }
    with(AdminCommands) { register() }
  }
}

interface Command : com.mojang.brigadier.Command<CommandSourceStack> {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }
  fun DSLCommandNode<CommandSourceStack>.register(): Unit

  companion object {
    const val SINGLE_SUCCESS = 1
    const val SINGLE_FAILURE = 0
  }
}