package ru.xllifi.rewards.sql

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.TextColumnType
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.json.json
import org.jetbrains.exposed.v1.json.jsonb
import java.nio.ByteBuffer
import java.util.*

class PlayerData(id: EntityID<UUID>) : Entity<UUID>(id) {
  companion object : EntityClass<UUID, PlayerData>(PlayerDatas)

  //  var playerUuid by PlayerDatas.playerUuid
  var collectedCalendarCells by PlayerDatas.collectedCalendarCells
  override fun toString(): String {
    return "PlayerData(id=$id, collectedCalendarCells=$collectedCalendarCells)"
  }
}

fun blobToUuid(blob: ExposedBlob): UUID {
  val bb = ByteBuffer.wrap(blob.bytes)
  val msb = bb.getLong()
  val lsb = bb.getLong()
  return UUID(msb, lsb)
}

fun uuidToBlob(uuid: UUID): ExposedBlob {
  val bb = ByteBuffer.wrap(ByteArray(16))
  bb.putLong(uuid.mostSignificantBits)
  bb.putLong(uuid.leastSignificantBits)
  return ExposedBlob(bb.array())
}

object PlayerDatas : IdTable<UUID>("t_player_data") {
  override val id: Column<EntityID<UUID>> = text("player_uuid")
    .transform<String, UUID>(
      wrap = { UUID.fromString(it) },
      unwrap = { it.toString() },
    )
    .entityId()
  override val primaryKey = PrimaryKey(id)

  val collectedCalendarCells: Column<Map<String, Set<String>>> = json("collected_calendar_cells", Json)
}
