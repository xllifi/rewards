package ru.xllifi.rewards.commands

import com.mojang.brigadier.context.CommandContext
import de.phyrone.brig.wrapper.DSLCommandNode
import de.phyrone.brig.wrapper.literal
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.commands.admin.AdminCommands
import ru.xllifi.rewards.commands.calendar.CalendarCommands
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.modId
import ru.xllifi.rewards.utils.plus

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
  }
}

object RewardsCommands : Command {
  override fun run(ctx: CommandContext<CommandSourceStack>): Int {
    ctx.source.sendSuccess({
      ctx.getServerAttachment().audiences.asNative(Main.globalConfig.prefix) + Component.literal(" ") + Component
        .translatable(
          "rewards.commands.root",
          Component
            .literal(FabricLoader.getInstance().getModContainer(modId).get().metadata.version.friendlyString)
            .withStyle(ChatFormatting.YELLOW)
        )
        .withStyle(ChatFormatting.GRAY)
    }, false)
    return Command.SINGLE_SUCCESS
  }

  override fun DSLCommandNode<CommandSourceStack>.register() {
    literal("calendar") {
      with(CalendarCommands) { register() }
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