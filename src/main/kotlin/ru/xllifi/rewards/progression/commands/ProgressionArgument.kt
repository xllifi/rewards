package ru.xllifi.rewards.progression.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.config.getServerAttachment

fun DSLCommandNode<CommandSourceStack>.progressionArgument(
  progressionArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(progressionArgumentName, StringArgumentType.string()) {
    suggest { progressionSuggestions(it) }
    setup()
  }

fun SuggestionsBuilder.progressionSuggestions(ctx: CommandContext<CommandSourceStack>) {
  val lastInputPart = ctx.input.split(' ').last()
  ctx.getServerAttachment().progressions
    .map { it.id }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}