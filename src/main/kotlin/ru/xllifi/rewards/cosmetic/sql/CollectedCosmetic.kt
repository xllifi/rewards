package ru.xllifi.rewards.cosmetic.sql

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.utils.plus
import java.util.UUID

class CollectedCosmetic(id: EntityID<CompositeID>) : CompositeEntity(id) {
  companion object : CompositeEntityClass<CollectedCosmetic>(CollectedCosmeticsTable)

  var playerUuid by CollectedCosmeticsTable.playerUuid
  var cosmeticKind by CollectedCosmeticsTable.cosmeticKind
  var cosmeticId by CollectedCosmeticsTable.cosmeticId
  var cosmeticCollection by CollectedCosmeticsTable.cosmeticCollection
  var isEquipped by CollectedCosmeticsTable.isEquipped
}

object CollectedCosmeticsTable : CompositeIdTable("t_collected_cosmetics") {
  val playerUuid = text("player_uuid")
    .transform(
      wrap = { UUID.fromString(it) },
      unwrap = { it.toString() },
    )
    .entityId()
  val cosmeticKind = enumeration<CosmeticKind>("cosmetic_kind").entityId()
  val cosmeticId = text("cosmetic_id").entityId()
  val cosmeticCollection = text("cosmetic_collection").nullable()
  val isEquipped = bool("is_equipped")

  override val primaryKey: PrimaryKey = PrimaryKey(playerUuid, cosmeticKind, cosmeticId)

  init {
    index(
      isUnique = true,
      columns = arrayOf(playerUuid, cosmeticCollection),
      filterCondition = {
        isEquipped.eq(true) + cosmeticCollection.isNotNull()
      }
    )
  }
}