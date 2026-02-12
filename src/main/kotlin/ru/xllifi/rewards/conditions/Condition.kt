package ru.xllifi.rewards.conditions

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.minecraft.ChatFormatting
import net.minecraft.server.level.ServerPlayer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

@Serializable
sealed interface Condition {
  @OptIn(InternalSerializationApi::class)
  val serialName: String
    get() = this::class.serializer().descriptor.serialName

  /**
   * @return `null` - pending, `true` - success, `false` - failure
   */
  fun status(player: ServerPlayer): Boolean?
  fun mark(player: ServerPlayer): MutableComponent =
    when (status(player)) {
      null -> Component.translatable("rewards.condition.mark.pending").append(" ").withStyle(ChatFormatting.GRAY)
      true -> Component.translatable("rewards.condition.mark.success").append(" ").withStyle(ChatFormatting.GREEN)
      false -> Component.translatable("rewards.condition.mark.failure").append(" ").withStyle(ChatFormatting.RED)
    }

  fun lore(player: ServerPlayer): List<Component>
}

