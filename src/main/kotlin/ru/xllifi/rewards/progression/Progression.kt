package ru.xllifi.rewards.progression

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.conditions.Condition
import ru.xllifi.rewards.rewards.Reward
import ru.xllifi.rewards.serializers.ResourceLocation
import ru.xllifi.rewards.serializers.text.Component
import kotlin.math.ceil

@Serializable
data class Progression(
  val id: String,
  val title: Component,
  val tiers: List<Tier>,

  @Transient
  val lines: Int = ceil(tiers.size / 7f).toInt(),
) {
  init {
    require(lines in 1..6) {
      "Make sure there is at least 1 but no more than 42 tiers."
    }
  }

  fun tierStatus(tier: Tier, player: ServerPlayer): Boolean? =
    tier.unlockCondition.status(player)

  @Serializable
  class Tier(
    val title: Component,
    val description: List<Component>,
    val collectionSound: ResourceLocation? = null,
    @Contextual val displayItem: ItemStack,
    val unlockCondition: Condition,
    val rewards: List<Reward>,
  )
}