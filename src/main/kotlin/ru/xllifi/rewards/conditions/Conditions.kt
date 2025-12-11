package ru.xllifi.rewards.conditions

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import ru.xllifi.rewards.logger
import ru.xllifi.rewards.serializers.time.InstantAsDay
import ru.xllifi.rewards.serializers.time.InstantAsUnix
import ru.xllifi.rewards.utils.getOrThrow
import kotlin.time.Clock
import kotlin.time.Instant

val conditionsSerializersModule = SerializersModule {
  polymorphic(Condition::class) {
    subclass(StatCondition::class)
    subclass(TimeUnixCondition::class)
    subclass(TimeDaysCondition::class)
    subclass(AllCondition::class)
    subclass(AnyCondition::class)
    subclass(SomeCondition::class)
  }
}

@Serializable
@Polymorphic
sealed class Condition {
  abstract fun isMet(player: ServerPlayer): Boolean
}

@Serializable
@SerialName("stat")
data class StatCondition(
  val statType: Type,
  val stat: String,
  val threshold: Int,
) : Condition() {
  @Serializable
  enum class Type {
    @SerialName("block_mined") BLOCK_MINED,
    @SerialName("item_crafted") ITEM_CRAFTED,
    @SerialName("item_used") ITEM_USED,
    @SerialName("item_broken") ITEM_BROKEN,
    @SerialName("item_picked_up") ITEM_PICKED_UP,
    @SerialName("item_dropped") ITEM_DROPPED,
    @SerialName("entity_killed") ENTITY_KILLED,
    @SerialName("entity_killed_by") ENTITY_KILLED_BY,
    @SerialName("custom") CUSTOM,
  }

  private fun getStatValue(player: ServerPlayer): Int {
    val statHandler = player.stats
    return when (statType) {
      Type.BLOCK_MINED -> {
        statHandler.getValue(
          Stats.BLOCK_MINED.get(
            BuiltInRegistries.BLOCK.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ITEM_CRAFTED -> {
        statHandler.getValue(
          Stats.ITEM_CRAFTED.get(
            BuiltInRegistries.ITEM.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ITEM_USED -> {
        statHandler.getValue(
          Stats.ITEM_USED.get(
            BuiltInRegistries.ITEM.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ITEM_BROKEN -> {
        statHandler.getValue(
          Stats.ITEM_BROKEN.get(
            BuiltInRegistries.ITEM.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ITEM_PICKED_UP -> {
        statHandler.getValue(
          Stats.ITEM_PICKED_UP.get(
            BuiltInRegistries.ITEM.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ITEM_DROPPED -> {
        statHandler.getValue(
          Stats.ITEM_DROPPED.get(
            BuiltInRegistries.ITEM.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ENTITY_KILLED -> {
        statHandler.getValue(
          Stats.ENTITY_KILLED.get(
            BuiltInRegistries.ENTITY_TYPE.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.ENTITY_KILLED_BY -> {
        statHandler.getValue(
          Stats.ENTITY_KILLED_BY.get(
            BuiltInRegistries.ENTITY_TYPE.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }

      Type.CUSTOM -> {
        statHandler.getValue(
          Stats.CUSTOM.get(
            BuiltInRegistries.CUSTOM_STAT.getOrThrow(
              ResourceLocation.parse(stat)
            )
          )
        )
      }
    }
  }

  override fun isMet(player: ServerPlayer): Boolean {
    val statValue = getStatValue(player)
    logger.info("Stat value $statValue, threshold $threshold")

    return statValue > threshold
  }
}

@Serializable
@SerialName("time_unix")
data class TimeUnixCondition(
  val unlockedAt: InstantAsUnix,
  val expiredAt: InstantAsUnix?,
): Condition() {
  override fun isMet(player: ServerPlayer): Boolean {
    val now = Clock.System.now()
    return now in unlockedAt..(expiredAt ?: Instant.DISTANT_FUTURE)
  }
}

@Serializable
@SerialName("time_days")
data class TimeDaysCondition(
  val unlockedAt: InstantAsDay,
  val expiredAt: InstantAsDay?,
): Condition() {
  override fun isMet(player: ServerPlayer): Boolean {
    val now = Clock.System.now()
    return now in unlockedAt..(expiredAt ?: Instant.DISTANT_FUTURE)
  }
}

@Serializable
@SerialName("all")
data class AllCondition(
  val conditions: List<Condition>
): Condition() {
  override fun isMet(player: ServerPlayer): Boolean = conditions.all { it.isMet(player) }
}

@Serializable
@SerialName("any")
data class AnyCondition(
  val conditions: List<Condition>
): Condition() {
  override fun isMet(player: ServerPlayer): Boolean = conditions.any { it.isMet(player) }
}

@Serializable
@SerialName("some")
data class SomeCondition(
  val conditions: List<Condition>,
  val threshold: Int,
): Condition() {
  override fun isMet(player: ServerPlayer): Boolean =
    conditions.filter { it.isMet(player) }.size > threshold
}