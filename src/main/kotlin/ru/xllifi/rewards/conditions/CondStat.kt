package ru.xllifi.rewards.conditions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stat
import net.minecraft.stats.Stats
import ru.xllifi.rewards.serializers.Identifier
import ru.xllifi.rewards.utils.extensions.getOrThrow

@Serializable
@SerialName("stat")
data class CondStat(
  val statType: Type,
  val stat: Identifier,
  val threshold: Int,
) : Condition {
  @Serializable
  enum class Type {
    @SerialName("block_mined")
    BLOCK_MINED,

    @SerialName("item_crafted")
    ITEM_CRAFTED,

    @SerialName("item_used")
    ITEM_USED,

    @SerialName("item_broken")
    ITEM_BROKEN,

    @SerialName("item_picked_up")
    ITEM_PICKED_UP,

    @SerialName("item_dropped")
    ITEM_DROPPED,

    @SerialName("entity_killed")
    ENTITY_KILLED,

    @SerialName("entity_killed_by")
    ENTITY_KILLED_BY,

    @SerialName("custom")
    CUSTOM,
  }

  private fun getStat(): Stat<*> {
    return when (statType) {
      Type.BLOCK_MINED -> {
        Stats.BLOCK_MINED.get(
          BuiltInRegistries.BLOCK.getOrThrow(stat)
        )
      }

      Type.ITEM_CRAFTED -> {
        Stats.ITEM_CRAFTED.get(
          BuiltInRegistries.ITEM.getOrThrow(stat)
        )
      }

      Type.ITEM_USED -> {
        Stats.ITEM_USED.get(
          BuiltInRegistries.ITEM.getOrThrow(stat)
        )
      }

      Type.ITEM_BROKEN -> {
        Stats.ITEM_BROKEN.get(
          BuiltInRegistries.ITEM.getOrThrow(stat)
        )
      }

      Type.ITEM_PICKED_UP -> {
        Stats.ITEM_PICKED_UP.get(
          BuiltInRegistries.ITEM.getOrThrow(stat)
        )
      }

      Type.ITEM_DROPPED -> {
        Stats.ITEM_DROPPED.get(
          BuiltInRegistries.ITEM.getOrThrow(stat)
        )
      }

      Type.ENTITY_KILLED -> {
        Stats.ENTITY_KILLED.get(
          BuiltInRegistries.ENTITY_TYPE.getOrThrow(stat)
        )
      }

      Type.ENTITY_KILLED_BY -> {
        Stats.ENTITY_KILLED_BY.get(
          BuiltInRegistries.ENTITY_TYPE.getOrThrow(stat)
        )
      }

      Type.CUSTOM -> {
        Stats.CUSTOM.get(
          BuiltInRegistries.CUSTOM_STAT.getOrThrow(stat)
        )
      }
    }
  }

  override fun status(player: ServerPlayer): Boolean? {
    val statValue = player.stats.getValue(getStat())
    return if (statValue >= threshold) true else null
  }

  override fun lore(player: ServerPlayer): List<Component> {
    val (displayTextKey, statTextKey) = Pair(
      "rewards.condition.stat." + when (this.statType) {
        Type.CUSTOM -> "custom"
        Type.BLOCK_MINED -> "block_mined"
        Type.ITEM_CRAFTED -> "item_crafted"
        Type.ITEM_USED -> "item_used"
        Type.ITEM_BROKEN -> "item_broken"
        Type.ITEM_PICKED_UP -> "item_picked_up"
        Type.ITEM_DROPPED -> "item_dropped"
        Type.ENTITY_KILLED -> "entity_killed"
        Type.ENTITY_KILLED_BY -> "entity_killed_by"
      },
      when (this.statType) {
        Type.CUSTOM -> "stat"
        Type.BLOCK_MINED -> "block"
        Type.ITEM_CRAFTED -> "item"
        Type.ITEM_USED -> "item"
        Type.ITEM_BROKEN -> "item"
        Type.ITEM_PICKED_UP -> "item"
        Type.ITEM_DROPPED -> "item"
        Type.ENTITY_KILLED -> "entity"
        Type.ENTITY_KILLED_BY -> "entity"
      } + ".${this.stat.namespace}.${this.stat.path}"
    )
    val stat = getStat()

    val component = this.mark(player).append(
      Component.translatable(
        displayTextKey,
        Component.translatable(statTextKey),
        stat.format(this.threshold)
      )
    )
    val statValue = player.stats.getValue(getStat())
    if (statValue < this.threshold) {
      component.append(
        Component.translatable(
          "rewards.condition.stat.x_more",
          stat.format(this.threshold - statValue)
        )
      )
    }

    return listOf(component)
  }
}