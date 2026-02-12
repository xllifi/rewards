package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

@Serializable
@SerialName("all")
data class MCondAll(
  val conditions: List<Condition>
) : Condition {
  override fun status(player: ServerPlayer): Boolean? =
    if (conditions.all { it.status(player) == true }) true else null

  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable("rewards.condition.all")
      )
    ) + conditions.map { condition ->
      condition.lore(player).map { component ->
        Component.literal("  ").append(component)
      }
    }.flatten()
}