package ru.xllifi.rewards.rewards

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.utils.displayNameNoBrackets
import ru.xllifi.rewards.utils.plus

@Serializable
@SerialName("item")
class RewItem(
  @Contextual val itemStack: ItemStack,
) : Reward {
  override fun grant(player: ServerPlayer) {
    player.addItem(itemStack.copy())
  }

  override fun lore(player: ServerPlayer): Component =
    mark.copy() + Component.translatable(
      "rewards.reward.item",
      itemStack.count,
      itemStack.displayNameNoBrackets,
    )
}