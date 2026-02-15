package ru.xllifi.rewards.rewards

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.utils.plus

@Serializable
@SerialName("xp")
class RewXp(
  val amount: Int,
  val xpUnit: XpUnit,
) : Reward {
  @Serializable
  enum class XpUnit {
    @SerialName("points")
    Points,

    @SerialName("levels")
    Levels,
  }

  override fun grant(player: ServerPlayer) {
    when (xpUnit) {
      XpUnit.Points ->
        player.giveExperiencePoints(amount)

      XpUnit.Levels ->
        player.giveExperienceLevels(amount)
    }
  }

  override fun lore(player: ServerPlayer): Component =
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