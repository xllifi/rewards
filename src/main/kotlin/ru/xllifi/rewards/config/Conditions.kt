package ru.xllifi.rewards.config

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.minecraft.ChatFormatting
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import ru.xllifi.rewards.logger
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.stats.Stat
import ru.xllifi.rewards.serializers.ResourceLocation
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.serializers.time.InstantAsUnix
import ru.xllifi.rewards.serializers.time.dayHumanReadable
import ru.xllifi.rewards.serializers.time.unixHumanReadable
import ru.xllifi.rewards.utils.getOrThrow
import kotlin.time.Clock

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

@Serializable
@SerialName("none")
data class NoCondition(
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

@Serializable
@SerialName("stat")
data class StatCondition(
  val statType: Type,
  val stat: ResourceLocation,
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
    logger.info("Stat value $statValue, threshold $threshold")

    return if (statValue > threshold) true else null
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

    return listOf(
      this.mark(player).append(
        Component.translatable(
          displayTextKey,
          Component.translatable(statTextKey),
          stat.format(this.threshold)
        )
      )
    )
  }
}

@Serializable
@SerialName("time_unix")
data class TimeUnixCondition(
  val unlockedAt: InstantAsUnix,
  val expiredAt: InstantAsUnix?,
) : Condition {
  override fun status(player: ServerPlayer): Boolean? {
    val now = Clock.System.now()
    return when {
      expiredAt != null && now > expiredAt -> false
      now > unlockedAt -> true
      else -> null
    }
  }

  override fun lore(player: ServerPlayer): List<Component> {
    val now = Clock.System.now()
    return listOf(
      this.mark(player).append(
        when {
          expiredAt != null && now > expiredAt ->
            Component.translatable(
              "rewards.condition.time.expired",
              this.expiredAt.unixHumanReadable(),
            )

          expiredAt != null && now > unlockedAt ->
            Component.translatable(
              "rewards.condition.time.wait_expirable",
              this.unlockedAt.unixHumanReadable(),
              this.expiredAt.unixHumanReadable(),
            )

          else ->
            Component.translatable(
              "rewards.condition.time.wait",
              this.unlockedAt.unixHumanReadable(),
            )
        }
      )
    )
  }
}

@Serializable
@SerialName("time_days")
data class TimeDaysCondition(
  val unlockedAt: InstantAsDay,
  val expiredAt: InstantAsDay?,
) : Condition {
  override fun status(player: ServerPlayer): Boolean? {
    val now = Clock.System.now()
    return when {
      expiredAt != null && now > expiredAt -> false
      expiredAt != null && now > unlockedAt -> null
      now > unlockedAt -> true
      else -> null
    }
  }

  override fun lore(player: ServerPlayer): List<Component> {
    val now = Clock.System.now()
    return listOf(
      this.mark(player).append(
        when {
          expiredAt != null && now > expiredAt ->
            Component.translatable(
              "rewards.condition.time.expired",
              this.expiredAt.dayHumanReadable(),
            )

          expiredAt != null && now > unlockedAt ->
            Component.translatable(
              "rewards.condition.time.wait_expirable",
              this.unlockedAt.dayHumanReadable(),
              this.expiredAt.dayHumanReadable(),
            )

          else ->
            Component.translatable(
              "rewards.condition.time.wait",
              this.unlockedAt.dayHumanReadable(),
            )
        }
      )
    )
  }
}

@Serializable
@SerialName("all")
data class AllCondition(
  val conditions: List<Condition>
) : Condition {
  override fun status(player: ServerPlayer): Boolean? =
    if (conditions.all { it.status(player) == true }) true else null

  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable("rewards.condition.all")
      )
    ) + conditions.map { condition ->
      condition.lore(player).map { component ->
        Component.literal("  ").append(component)
      }
    }.flatten()
}

@Serializable
@SerialName("any")
data class AnyCondition(
  val conditions: List<Condition>
) : Condition {
  override fun status(player: ServerPlayer): Boolean? =
    if (conditions.any { it.status(player) == true }) true else null

  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable("rewards.condition.any")
      )
    ) + conditions.map { condition ->
      condition.lore(player).map { component ->
        Component.literal("  ").append(component)
      }
    }.flatten()
}

@Serializable
@SerialName("some")
data class SomeCondition(
  val conditions: List<Condition>,
  val threshold: Int,
) : Condition {
  override fun status(player: ServerPlayer): Boolean? {
    val statuses = conditions.map { it.status(player) }
    return if (statuses.filter { it == true }.size >= threshold) {
      true
    } else {
      if (statuses.filter { it != false }.size < threshold)
        false
      else
        null
    }
  }

  override fun lore(player: ServerPlayer): List<Component> =
    listOf(
      this.mark(player).append(
        Component.translatable(
          "rewards.condition.some",
          threshold
        )
      )
    ) + conditions.map { condition ->
      condition.lore(player).map { component ->
        Component.literal("  ").append(component)
      }
    }.flatten()
}