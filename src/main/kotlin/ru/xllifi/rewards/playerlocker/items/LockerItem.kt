package ru.xllifi.rewards.playerlocker.items

import eu.pb4.sgui.api.elements.GuiElementBuilder
import kotlinx.serialization.Serializable
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.server.level.ServerPlayer
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import ru.xllifi.rewards.playerlocker.sql.CollectedLockerItem
import ru.xllifi.rewards.playerlocker.sql.CollectedLockerItemTable
import ru.xllifi.rewards.utils.plus
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

object LockerRegistry {
  val typeToKind = mutableMapOf<KClass<out LockerItem>, LockerItemKind>()

  init {
    LockerItem::class.sealedSubclasses.forEach { kClass ->
      if (kClass.findAnnotation<RegisteredLockerItem>() == null) {
        throw IllegalStateException(
          "Developer Error: ${kClass.simpleName} must be annotated with @RegisteredLockerItem"
        )
      }
    }
  }

  fun getKind(kClass: KClass<out LockerItem>): LockerItemKind {
    return typeToKind.getOrPut(kClass) {
      val annotation = kClass.findAnnotation<RegisteredLockerItem>()
        ?: throw IllegalArgumentException("${kClass.simpleName} is not annotated with @RegisteredLockerItem")

      val kindClass = annotation.kindClass

      kindClass.objectInstance ?: kindClass.createInstance()
    }
  }
}

@Target(AnnotationTarget.CLASS)
annotation class RegisteredLockerItem(val kindClass: KClass<out LockerItemKind>)

@Serializable
sealed interface LockerItemKind {
  val name: String
  abstract fun getGuiElementBuilder(): GuiElementBuilder
}

@Serializable
sealed class LockerItem() {
  abstract val kind: LockerItemKind
  abstract fun getGuiElementBuilder(audiences: MinecraftServerAudiences): GuiElementBuilder

  private fun thisItem(player: ServerPlayer): Op<Boolean> =
    CollectedLockerItemTable.playerUuid.eq(player.uuid) +
      CollectedLockerItemTable.kind.eq(this.kind) +
      CollectedLockerItemTable.item.eq(this)

  /**
   * @return `null` - not found, `false` - no, `true` - yes
   */
  fun isEquippedFor(player: ServerPlayer): Boolean? {
    val item = this
    return transaction {
      CollectedLockerItem.find { thisItem(player) }.firstOrNull()?.equipped
    }
  }

  fun setEquippedFor(player: ServerPlayer, to: Boolean) {
    val item = this
    return transaction {
      val found = CollectedLockerItem.find { thisItem(player) }.firstOrNull()

      if (found != null) {
        CollectedLockerItem.findSingleByAndUpdate(thisItem(player)) {
          it.equipped = to
        }
      } else {
        CollectedLockerItem.new {
          this.playerUuid = player.uuid
          this.kind = item.kind
          this.item = item
          this.equipped = to
        }
      }
    }
  }

  fun addItemFor(player: ServerPlayer) {
    val item = this
    return transaction {
      val found = CollectedLockerItem.find { thisItem(player) }.firstOrNull()

      if (found == null) {
        CollectedLockerItem.new {
          this.playerUuid = player.uuid
          this.kind = item.kind
          this.item = item
          this.equipped = false
        }
      }
    }
  }
}