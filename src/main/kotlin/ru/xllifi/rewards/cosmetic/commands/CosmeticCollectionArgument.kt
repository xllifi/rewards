package ru.xllifi.rewards.cosmetic.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import de.phyrone.brig.wrapper.DSLCommandNode
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.CosmeticDef
import kotlin.text.startsWith

fun DSLCommandNode<CommandSourceStack>.cosmeticCollectionArgument(
  cosmeticCollectionArgumentName: String,
  setup: DSLCommandNode<CommandSourceStack>.() -> Unit
) =
  argument(cosmeticCollectionArgumentName, StringArgumentType.string()) {
    suggest { cosmeticCollectionSuggestions(it) }
    setup()
  }

fun SuggestionsBuilder.cosmeticCollectionSuggestions(ctx: CommandContext<CommandSourceStack>) {
  val lastInputPart = ctx.input.split(' ').last()
  ctx.getServerAttachment().cosmetics.values
    .flatMap { x -> x.values.map { it.collection } }
    .filterNotNull()
    .toSet()
    .filter { it.startsWith(lastInputPart) }
    .forEach { suggest(it) }
}

fun CommandContext<CommandSourceStack>.getCosmeticCollectionArgument(
  cosmeticCollectionArgumentName: String
): List<CosmeticDef> {
  val cosmeticCollectionName = StringArgumentType.getString(this, cosmeticCollectionArgumentName)
  val cosmetics = this.getServerAttachment().cosmetics.values.flatMap { x -> x.values.filter { it.collection != null && it.collection == cosmeticCollectionName } }
  return cosmetics
}