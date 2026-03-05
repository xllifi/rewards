package ru.xllifi.rewards.locker.sql

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import ru.xllifi.rewards.locker.items.LockerItem
import ru.xllifi.rewards.locker.items.LockerItemKind
import java.util.UUID

class CollectedLockerItem(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<CollectedLockerItem>(CollectedLockerItemTable)

  var playerUuid by CollectedLockerItemTable.playerUuid
  var kind by CollectedLockerItemTable.kind
  var item by CollectedLockerItemTable.item
  var equipped by CollectedLockerItemTable.equipped

  override fun toString(): String =
    "LockerItem(playerUuid=$playerUuid, kind=$kind, item=$item, equipped=$equipped)"
}

object CollectedLockerItemTable : IntIdTable("t_collected_locker_items") {
  val playerUuid: Column<UUID> = text("player_uuid")
    .transform(
      wrap = { UUID.fromString(it) },
      unwrap = { it.toString() },
    )
  val kind: Column<LockerItemKind> = text("kind")
    .transform(
      wrap = { Json.decodeFromString(it) },
      unwrap = { Json.encodeToString(it) },
    )
  val item: Column<LockerItem> = text("item")
    .transform(
      wrap = { Json.decodeFromString(it) },
      unwrap = { Json.encodeToString(it) },
    )
  val equipped: Column<Boolean> = bool("equipped")
}