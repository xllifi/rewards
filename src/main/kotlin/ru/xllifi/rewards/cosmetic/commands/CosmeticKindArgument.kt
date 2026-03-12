package ru.xllifi.rewards.cosmetic.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.pearx.kasechange.toPascalCase
import ru.xllifi.rewards.cosmetic.CosmeticKind

fun DSLCommandNode<CommandSourceStack>.cosmeticKindArgument(
  cosmeticKindArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(cosmeticKindArgumentName, StringArgumentType.string()) {
    suggest { cosmeticKindSuggestions(it) }
    setup()
  }

fun SuggestionsBuilder.cosmeticKindSuggestions(ctx: CommandContext<CommandSourceStack>) {
  val lastInputPart = ctx.input.split(' ').last()
  CosmeticKind.entries
    .map { it.snakeCaseName() }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}

fun CommandContext<CommandSourceStack>.getCosmeticKindArgument(cosmeticKindArgumentName: String): CosmeticKind {
  try {
    return CosmeticKind.valueOf(StringArgumentType.getString(this, cosmeticKindArgumentName).toPascalCase())
  } catch (_: IllegalArgumentException) {
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such cosmetic kind $cosmeticKindArgumentName")
  }
}