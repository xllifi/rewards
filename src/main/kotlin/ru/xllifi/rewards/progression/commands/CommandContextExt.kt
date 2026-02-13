package ru.xllifi.rewards.progression.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.progression.Progression

fun CommandContext<CommandSourceStack>.getProgressionArgument(progressionArgumentName: String): Progression {
  val progressionId = StringArgumentType.getString(this, progressionArgumentName)
  val progression = getServerAttachment().progressions.firstOrNull { it.id == progressionId }
  if (progression == null)
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException()
      .create("No such calendar with ID $progressionId")
  return progression
}

fun CommandContext<CommandSourceStack>.getTierIdxArgument(
  progressionArgumentName: String,
  tierArgumentName: String
): Int {
  val progression = this.getProgressionArgument(progressionArgumentName)
  val tierIdx = IntegerArgumentType.getInteger(this, tierArgumentName)
  if (tierIdx >= progression.tiers.size) {
    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Tier index too big. Max ${progression.tiers.size - 1}")
  }
  return tierIdx
}

fun CommandContext<CommandSourceStack>.getProgressionAndTierIdxArguments(
  progressionArgumentName: String,
  tierArgumentName: String
): Pair<Progression, Int> {
  val progression = this.getProgressionArgument(progressionArgumentName)
  val tierIdx = this.getTierIdxArgument(progressionArgumentName, tierArgumentName)
  return progression to tierIdx
}