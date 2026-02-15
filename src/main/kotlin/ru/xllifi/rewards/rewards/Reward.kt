package ru.xllifi.rewards.rewards

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

@Serializable
sealed interface Reward {
  fun grant(player: ServerPlayer)
  fun lore(player: ServerPlayer): Component
}

val mark: Component = Component.translatable("rewards.generic.mark").append(" ").withStyle(ChatFormatting.GRAY)

fun List<Reward>.grant(player: ServerPlayer) = this.forEach { it.grant(player) }
