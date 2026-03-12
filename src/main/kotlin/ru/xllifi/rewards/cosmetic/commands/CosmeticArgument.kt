package ru.xllifi.rewards.cosmetic.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import net.pearx.kasechange.toPascalCase
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.CosmeticDef
import ru.xllifi.rewards.cosmetic.CosmeticKind

fun DSLCommandNode<CommandSourceStack>.cosmeticArgument(
  cosmeticKindArgumentName: String,
  cosmeticArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(cosmeticArgumentName, StringArgumentType.string()) {
    suggest { cosmeticSuggestions(cosmeticKindArgumentName, it) }
    setup()
  }

fun SuggestionsBuilder.cosmeticSuggestions(cosmeticKindArgumentName: String, ctx: CommandContext<CommandSourceStack>) {
  val cosmeticKind = ctx.getCosmeticKindArgument(cosmeticKindArgumentName)
  val lastInputPart = ctx.input.split(' ').last()
  (ctx.getServerAttachment().cosmetics[cosmeticKind] ?: return).values
    .map { it.id }
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}


fun CommandContext<CommandSourceStack>.getCosmeticArgument(
  cosmeticKindArgumentName: String,
  cosmeticArgumentName: String
): Pair<CosmeticKind, CosmeticDef> {
  val cosmeticKind = try {
    CosmeticKind.valueOf(StringArgumentType.getString(this, cosmeticKindArgumentName).toPascalCase())
  } catch (_: IllegalArgumentException) {
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such cosmetic kind $cosmeticKindArgumentName")
  }
  val cosmeticName = StringArgumentType.getString(this, cosmeticArgumentName)
  val cosmetic = this.getServerAttachment().cosmetics[cosmeticKind]?.values?.firstOrNull { it.id == cosmeticName }
    ?: throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such cosmetic with kind $cosmeticKind and ID $cosmeticName")
  return cosmeticKind to cosmetic
}