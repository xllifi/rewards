package ru.xllifi.rewards.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.utils.displayNameNoBrackets
import ru.xllifi.rewards.utils.plus

val mark: Component = Component.translatable("rewards.generic.mark").append(" ").withStyle(ChatFormatting.GRAY)

@Serializable
sealed interface Reward {
  fun grant(player: ServerPlayer)
  fun lore(): Component
}

fun List<Reward>.grant(player: ServerPlayer) = this.forEach { it.grant(player) }

@Serializable
@SerialName("item")
class ItemReward(
  @Contextual val itemStack: ItemStack,
) : Reward {
  override fun grant(player: ServerPlayer) {
    player.addItem(itemStack)
  }

  override fun lore(): Component =
    mark.copy() + Component.translatable(
      "rewards.reward.item",
      itemStack.count,
      itemStack.displayNameNoBrackets,
    )
}

@Serializable
@SerialName("xp")
class XpReward(
  val amount: Int,
  val xpUnit: XpUnit,
) : Reward {
  override fun grant(player: ServerPlayer) {
    when (xpUnit) {
      XpUnit.Points ->
        player.giveExperiencePoints(amount)

      XpUnit.Levels ->
        player.giveExperienceLevels(amount)
    }
  }

  override fun lore(): Component =
    mark.copy() + Component.translatable(
      "rewards.reward.xp",
      amount,
      Component.translatable(
        "rewards.reward.xp." + when (xpUnit) {
          XpUnit.Points -> "points"
          XpUnit.Levels -> "levels"
        }
      )
    )
}

@Serializable
enum class XpUnit {
  @SerialName("points")
  Points,

  @SerialName("levels")
  Levels,
}