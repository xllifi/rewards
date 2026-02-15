package ru.xllifi.rewards.rewards

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.playerlocker.items.LockerItem
import ru.xllifi.rewards.playerlocker.items.PrefixLockerItem
import ru.xllifi.rewards.playerlocker.items.SuffixLockerItem
import ru.xllifi.rewards.utils.plus

@Serializable
@SerialName("locker_item")
class RewLockerItem(
  val lockerItem: LockerItem,
) : Reward {
  override fun grant(player: ServerPlayer) =
    lockerItem.addItemFor(player)

  override fun lore(player: ServerPlayer): Component =
    mark.copy() + Component.translatable(
      "rewards.reward.locker_item.${lockerItem.kind.name}",
      when (lockerItem) {
        is PrefixLockerItem -> lockerItem.getNative(player)
        is SuffixLockerItem -> lockerItem.getNative(player)
      }
    )
}