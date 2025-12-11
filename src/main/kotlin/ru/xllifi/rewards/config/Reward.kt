package ru.xllifi.rewards.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

val rewardsSerializersModule = SerializersModule {
  polymorphic(Reward::class) {
    subclass(ItemReward::class)
    subclass(XpReward::class)
  }
}

@Serializable
@Polymorphic
sealed class Reward {
  abstract fun grant(player: ServerPlayer)
}

fun List<Reward>.grant(player: ServerPlayer) = this.forEach { it.grant(player) }

@Serializable
@SerialName("item")
class ItemReward(
  @Contextual val itemStack: ItemStack,
) : Reward() {
  override fun grant(player: ServerPlayer) {
    player.addItem(itemStack)
  }
}

@Serializable
@SerialName("xp")
class XpReward(
  val amount: Int,
  val xpUnit: XpUnit,
) : Reward() {
  override fun grant(player: ServerPlayer) {
    when (xpUnit) {
      XpUnit.Points ->
        player.giveExperiencePoints(amount)

      XpUnit.Levels ->
        player.giveExperienceLevels(amount)
    }
  }
}

@Serializable
enum class XpUnit {
  @SerialName("points")
  Points,
  @SerialName("levels")
  Levels,
}