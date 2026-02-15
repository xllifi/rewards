package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.serializers.time.dayHumanReadable
import kotlin.time.Clock

@Serializable
@SerialName("time_days")
data class CondTimeDays(
  val unlockedAt: InstantAsDay,
  val expiredAt: InstantAsDay?,
) : Condition {
  override fun status(player: ServerPlayer): Boolean? {
    val now = Clock.System.now()
    return when {
      expiredAt != null && now > expiredAt -> false
      now > unlockedAt -> true
      else -> null
    }
  }

  override fun lore(player: ServerPlayer): List<Component> {
    val now = Clock.System.now()
    return listOf(
      this.mark(player).append(
        when {
          expiredAt != null && now > expiredAt ->
            Component.translatable(
              "rewards.condition.time.expired",
              this.expiredAt.dayHumanReadable(),
            )

          expiredAt != null && now > unlockedAt ->
            Component.translatable(
              "rewards.condition.time.wait_expirable",
              this.unlockedAt.dayHumanReadable(),
              this.expiredAt.dayHumanReadable(),
            )

          else ->
            Component.translatable(
              "rewards.condition.time.wait",
              this.unlockedAt.dayHumanReadable(),
            )
        }
      )
    )
  }
}