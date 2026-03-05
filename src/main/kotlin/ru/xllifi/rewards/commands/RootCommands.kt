package ru.xllifi.rewards.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import de.phyrone.brig.wrapper.literal
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.calendar.commands.CalendarCommands
import ru.xllifi.rewards.commands.admin.AdminCommands
import ru.xllifi.rewards.mainmenu.MainMenuGui
import ru.xllifi.rewards.locker.commands.LockerCommands
import ru.xllifi.rewards.progression.commands.ProgressionCommands

fun registerCommands() {
  CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
    dispatcher.literal("rewards") {
      executes { RewardsCommands.run(it) }
      with(RewardsCommands) { register() }
      if (FabricLoader.getInstance().isDevelopmentEnvironment) {
        with(DebugCommands) { register() }
      }
    }
    dispatcher.literal("calendar") {
      with(CalendarCommands) { register() }
    }
    dispatcher.literal("progression") {
      with(ProgressionCommands) { register() }
    }
    dispatcher.literal("locker") {
      with(LockerCommands) { register() }
    }
  }
}

object RewardsCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    MainMenuGui(ctx.source.playerOrException)
    return Command.SINGLE_SUCCESS
  }

  // TODO: discover() command. New argument type for discovery type (calendar or progression)

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("calendar") {
      with(CalendarCommands) { register() }
    }
    literal("progression") {
      with(ProgressionCommands) { register() }
    }
    literal("locker") {
      with(LockerCommands) { register() }
    }
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