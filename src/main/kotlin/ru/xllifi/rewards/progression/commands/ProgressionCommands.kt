package ru.xllifi.rewards.progression.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.mainmenu.DiscoverProgressionsGui
import ru.xllifi.rewards.progression.ui.ProgressionScreen

object ProgressionCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun open(ctx: CommandContext<CommandSourceStack>): Int {
    val progression = ctx.getProgressionArgument("progression")
    val screen = ProgressionScreen(progression, ctx.source.playerOrException)
    screen.open()

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("open") {
      progressionArgument("progression") {
        executes { ctx -> open(ctx) }
      }
    }
  }
}
