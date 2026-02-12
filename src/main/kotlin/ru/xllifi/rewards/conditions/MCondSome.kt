package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

@Serializable
@SerialName("some")
data class MCondSome(
  val conditions: List<Condition>,
  val threshold: Int,
) : Condition {
  override fun status(player: ServerPlayer): Boolean? {
    val statuses = conditions.map { it.status(player) }
    return if (statuses.filter { it == true }.size >= threshold) {
      true
    } else {
      if (statuses.filter { it != false }.size < threshold)
        false
      else
        null
    }
  }

  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable(
          "rewards.condition.some",
          threshold
        )
      )
    ) + conditions.map { condition ->
      condition.lore(player).map { component ->
        Component.literal("  ").append(component)
      }
    }.flatten()
}