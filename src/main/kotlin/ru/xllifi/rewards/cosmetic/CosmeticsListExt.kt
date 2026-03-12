package ru.xllifi.rewards.cosmetic

import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmetic
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmeticsTable
import ru.xllifi.rewards.utils.plus

fun List<CosmeticDef>.getCollectedBy(player: ServerPlayer): List<CollectedCosmetic> {
  val kinds = this.map { it.kind }.toSet()
  val ids = this.map { it.id }.toSet()
  return transaction(Main.database) {
    CollectedCosmetic.find {
      CollectedCosmeticsTable.playerUuid.eq(player.uuid) +
      CollectedCosmeticsTable.cosmeticKind.inList(kinds) +
      CollectedCosmeticsTable.cosmeticId.inList(ids)
    }.toList()
  }
}