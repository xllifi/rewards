package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.serializers.time.InstantAsUnix
import ru.xllifi.rewards.serializers.time.unixHumanReadable
import kotlin.time.Clock

@Serializable
@SerialName("time_unix")
data class CondTimeUnix(
  val unlockedAt: InstantAsUnix,
  val expiredAt: InstantAsUnix?,
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
              this.expiredAt.unixHumanReadable(),
            )

          expiredAt != null && now > unlockedAt ->
            Component.translatable(
              "rewards.condition.time.wait_expirable",
              this.unlockedAt.unixHumanReadable(),
              this.expiredAt.unixHumanReadable(),
            )

          else ->
            Component.translatable(
              "rewards.condition.time.wait",
              this.unlockedAt.unixHumanReadable(),
            )
        }
      )
    )
  }
}