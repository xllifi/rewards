package ru.xllifi.rewards.rewards

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.utils.extensions.displayNameNoBrackets

@Serializable
@SerialName("item")
class RewItem(
  @Contextual val itemStack: ItemStack,
) : Reward {
  override fun grant(player: ServerPlayer) {
    val itemStack = itemStack.copy()

    val added = player.inventory.add(itemStack)
    if (!added) {
      val drop = player.drop(itemStack, false)
      if (drop != null) {
        drop.setNoPickUpDelay()
        drop.setTarget(player.uuid)
      }
    }
  }

  override fun lore(player: ServerPlayer): Component =
    mark.copy().append(
      Component.translatable(
        "rewards.reward.item",
        itemStack.count,
        itemStack.displayNameNoBrackets,
      )
    )
}