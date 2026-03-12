package ru.xllifi.rewards.cosmetic.ui

import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.DyedItemColor
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.AffixPlaceholders
import ru.xllifi.rewards.cosmetic.CosmeticDef
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.cosmetic.getCollectedBy
import ru.xllifi.rewards.cosmetic.kinds.AffixCosmeticDef
import ru.xllifi.rewards.utils.ui.PagedGui

class CollectedCosmeticsOfKindGui(
  val cosmeticKind: CosmeticKind,
  player: ServerPlayer,
  callback: () -> Unit,
) : PagedGui(
  player,
  callback,
) {
  val attachment = player.level().server.getServerAttachment()

  fun getFreshCosmeticsToEquippedMap(): Map<CosmeticDef, Boolean> {
    val registeredCosmetics = attachment.cosmetics[cosmeticKind]?.values?.toList() ?: emptyList()
    val collectedCosmetics = registeredCosmetics.getCollectedBy(player)

    val equippedById = collectedCosmetics.associate { it.cosmeticId.value to it.isEquipped }
    val collectedIds = collectedCosmetics.map { it.cosmeticId.value }
    val registeredCollectedCosmetics = registeredCosmetics.filter { collectedIds.contains(it.id) }

    return registeredCollectedCosmetics.associateWith {
      equippedById[it.id]
        ?: throw IllegalStateException("Cosmetic ${it.kind}:${it.id} should be collected but is not!")
    }
  }

  var cosmeticsToEquippedMap: Map<CosmeticDef, Boolean> = getFreshCosmeticsToEquippedMap()

  override val pageAmount: Int
    get() = cosmeticsToEquippedMap.size / PAGE_SIZE

  init {
    this.title = Component.translatable("Your collected ${cosmeticKind.name}s")
    this.refreshOpen()
  }

  override fun getElement(index: Int): DisplayElement? {
    if (index >= cosmeticsToEquippedMap.size) {
      return null
    }

    val cosmeticDef = cosmeticsToEquippedMap.keys.toList()[index]
    var builder = cosmeticDef.kind.getGuiElementBuilder()
      .setItemName(
        Component.translatable(
          "cosmetic.rewards.${cosmeticKind.snakeCaseName()}",
          cosmeticDef.getDisplayName(attachment.audiences)
        )
      )
      .setLore(
        listOf(
          Component.empty(),
          Component.translatable("gui.rewards.locker.equipped.${cosmeticsToEquippedMap[cosmeticDef] ?: false}")
            .withStyle(ChatFormatting.GRAY),
          Component.empty(),
          Component.translatable(
            "gui.rewards.locker.prompt",
            Component.translatable("gui.rewards.locker.prompt.to_${cosmeticsToEquippedMap[cosmeticDef]?.not() ?: true}")
          ).withStyle(ChatFormatting.YELLOW),
        )
      )
      .setCallback { _ ->
        cosmeticDef.updateOrCreateFor(player, (cosmeticsToEquippedMap[cosmeticDef]?.not()) ?: false)
        AffixPlaceholders.updateCacheFor(player, cosmeticKind)
        cosmeticsToEquippedMap = getFreshCosmeticsToEquippedMap()
        updateDisplay()
      }

    if (cosmeticDef is AffixCosmeticDef && cosmeticDef.component.color() != null) {
      builder = builder.setComponent(DataComponents.DYED_COLOR, DyedItemColor(cosmeticDef.component.color()!!.value()))
    }
    // TODO: (P3) toggle equip status. Remember to update cache!
    return DisplayElement.of(builder.build())
  }

}