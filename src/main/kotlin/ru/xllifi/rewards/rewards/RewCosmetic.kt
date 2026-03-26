package ru.xllifi.rewards.rewards

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.CosmeticDef
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.cosmetic.kinds.AffixCosmeticDef

@Serializable
@SerialName("cosmetic")
class RewCosmetic(
  val kind: CosmeticKind,
  val id: String,
) : Reward {
  fun getCosmeticDef(player: ServerPlayer): CosmeticDef =
    player.level().server.getServerAttachment().cosmetics[kind]?.get(id)
      ?: throw IllegalStateException("No cosmetics registered of kind $kind and id $id! (While parsing $this)")

  override fun grant(player: ServerPlayer) =
    getCosmeticDef(player).updateOrCreateFor(player, false)

  override fun lore(player: ServerPlayer): Component {
    val cosmeticDef = getCosmeticDef(player)
    return mark.copy().append(
      Component.translatable(
        "cosmetic.rewards.${cosmeticDef.kind.snakeCaseName()}",
        when (cosmeticDef) {
          is AffixCosmeticDef -> cosmeticDef.asNative(player.level().server.getServerAttachment().audiences)
          else -> throw IllegalStateException("Unknown cosmeticDef type ${cosmeticDef::class.simpleName}")
        }
      )
    )
  }
}