package ru.xllifi.rewards.cosmetic

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.cosmetic.kinds.PrefixCosmeticDef
import ru.xllifi.rewards.cosmetic.kinds.SuffixCosmeticDef
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmetic
import ru.xllifi.rewards.cosmetic.sql.CollectedCosmeticsTable

@Polymorphic
interface CosmeticDef {
  companion object {
    val serializersModule = SerializersModule {
      polymorphic(CosmeticDef::class) {
        subclass(PrefixCosmeticDef::class)
        subclass(SuffixCosmeticDef::class)
      }
    }
  }

  val id: String
  val kind: CosmeticKind
  val shouldCountInTotal: Boolean
  val collection: String?

  fun getDisplayName(audiences: MinecraftServerAudiences): Component

  fun updateOrCreateFor(player: ServerPlayer, equipped: Boolean) {
    return transaction(Main.database) {
      // unequip all of collection
      if (this@CosmeticDef.collection != null) {
        CollectedCosmeticsTable.update(
          where = { CollectedCosmeticsTable.cosmeticCollection eq this@CosmeticDef.collection },
          body = { it[isEquipped] = false }
        )
      }
      // equip one of that collection
      CollectedCosmeticsTable.upsert {
        it[playerUuid] = player.uuid
        it[cosmeticKind] = this@CosmeticDef.kind
        it[cosmeticId] = this@CosmeticDef.id
        it[cosmeticCollection] = this@CosmeticDef.collection
        it[isEquipped] = equipped
      }
    }
  }
}