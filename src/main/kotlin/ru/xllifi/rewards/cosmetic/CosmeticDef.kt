package ru.xllifi.rewards.cosmetic

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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

  fun getDisplayName(audiences: MinecraftServerAudiences): Component

  fun idFor(player: ServerPlayer) =
    CompositeID {
      with(CollectedCosmeticsTable) {
        it[playerUuid] = player.uuid
        it[cosmeticKind] = this@CosmeticDef.kind
        it[cosmeticId] = this@CosmeticDef.id
      }
    }

  fun isEquippedFor(player: ServerPlayer): Boolean {
    return transaction(Main.database) {
      CollectedCosmetic.findById(idFor(player)) != null
    }
  }

  fun updateOrCreateFor(player: ServerPlayer, equipped: Boolean) {
    return transaction(Main.database) {
      CollectedCosmeticsTable.upsert {
        it[playerUuid] = player.uuid
        it[cosmeticKind] = this@CosmeticDef.kind
        it[cosmeticId] = this@CosmeticDef.id
        it[isEquipped] = equipped
      }
    }
  }
}