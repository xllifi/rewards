package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

@Serializable
@SerialName("none")
data class CondNone(
  val isMet: Boolean
) : Condition {
  override fun status(player: ServerPlayer): Boolean = isMet
  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable("rewards.condition.none")
      )
    )
}