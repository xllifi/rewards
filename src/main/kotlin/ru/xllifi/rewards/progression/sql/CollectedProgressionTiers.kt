package ru.xllifi.rewards.progression.sql

import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.progression.Progression
import ru.xllifi.rewards.utils.plus
import java.util.UUID

class CollectedProgressionTiers(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CollectedProgressionTiers>(CollectedProgressionTiersTable)

  var playerUuid by CollectedProgressionTiersTable.playerUuid
  var progressionId by CollectedProgressionTiersTable.progressionId
  var tierIndex by CollectedProgressionTiersTable.tierIndex

  override fun toString(): String =
    "TrackedProgression(playerUuid=$playerUuid, progressionId=$progressionId, tierIndex=$tierIndex)"
}

object CollectedProgressionTiersTable : IntIdTable("t_collected_progression_tiers") {
  val playerUuid: Column<UUID> = text("player_uuid")
    .transform<String, UUID>(
      wrap = { UUID.fromString(it) },
      unwrap = { it.toString() },
    )
  val progressionId: Column<String> = text("progression_id")
  val tierIndex: Column<Int> = integer("tier_index")
}

fun Progression.getCollectedTierIndexes(player: ServerPlayer): List<Int> {
  val progression = this
  return transaction(Main.database) {
    addLogger(StdOutSqlLogger)
    CollectedProgressionTiers.find {
      CollectedProgressionTiersTable.playerUuid.eq(player.uuid) +
      CollectedProgressionTiersTable.progressionId.eq(progression.id)
    }.map { it.tierIndex }
  }
}

fun Progression.setTierCollection(player: ServerPlayer, tierIndex: Int, to: Boolean) {
  val progression = this
  require(tierIndex < progression.tiers.size) {
    "Tier index is bigger than tiers list size"
  }
  transaction(Main.database) {
    addLogger(StdOutSqlLogger)
    if (to == true) {
      CollectedProgressionTiers.new {
        playerUuid = player.uuid
        progressionId = progression.id
        this.tierIndex = tierIndex
      }
    } else {
      CollectedProgressionTiers.find {
        CollectedProgressionTiersTable.playerUuid.eq(player.uuid) +
        CollectedProgressionTiersTable.progressionId.eq(progression.id) +
        CollectedProgressionTiersTable.tierIndex.eq(tierIndex)
      }.firstOrNull()?.delete()
    }
  }
}