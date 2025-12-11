package ru.xllifi.rewards.config

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.conditions.Condition
import ru.xllifi.rewards.serializers.text.Component
import ru.xllifi.rewards.serializers.time.InstantAsDay

@Serializable
data class Progression(
  val id: String,
  val title: Component,
  val tiers: List<Tier>
) {
  @Serializable
  sealed class Tier(
    val title: Component,
    val description: List<Component>,
    @Contextual val displayItem: ItemStack,
    val unlockCondition: Condition,
    val rewards: List<Reward>,
  )
}