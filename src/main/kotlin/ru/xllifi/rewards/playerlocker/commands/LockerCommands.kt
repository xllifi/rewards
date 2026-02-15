package ru.xllifi.rewards.playerlocker.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.playerlocker.ui.LockerScreen
import ru.xllifi.rewards.progression.ui.DiscoverProgressionsScreen
import ru.xllifi.rewards.progression.ui.ProgressionScreen

object LockerCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    throw IllegalStateException("${this::class.simpleName} cannot be run.")
  }

  fun open(ctx: CommandContext<CommandSourceStack>): Int {
    val screen = LockerScreen(ctx.source.playerOrException)
    screen.open()

    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
//    literal("open") {
//    }
    executes { ctx -> open(ctx) }
  }
}
