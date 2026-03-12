package ru.xllifi.rewards.cosmetic.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.commands.Command
import ru.xllifi.rewards.cosmetic.ui.LockerGui

object CosmeticCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    LockerGui(ctx.source.playerOrException).open()
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    executes { ctx -> run(ctx) }
  }
}