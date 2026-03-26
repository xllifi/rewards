package ru.xllifi.rewards.cosmetic

import eu.pb4.placeholders.api.PlaceholderResult
import eu.pb4.placeholders.api.Placeholders
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import ru.xllifi.rewards.Main
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.kinds.AffixCosmeticDef
import ru.xllifi.rewards.utils.idOf
import java.util.UUID

object AffixPlaceholders {
  val cache: MutableMap<CosmeticKind, MutableMap<UUID, Component>> =
    CosmeticKind.entries.associateWith { mutableMapOf<UUID, Component>() }.toMutableMap()

  fun updateCacheFor(player: ServerPlayer, kind: CosmeticKind): MutableMap<UUID, Component> {
    Main.logger.info("Updating cache for ${player.scoreboardName} kind $kind")
    val attachment = player.level().server.getServerAttachment()
    val cosmeticsOfKind = attachment.cosmetics[kind] ?: emptyMap()
    val equippedCollected = cosmeticsOfKind.values
      .toList()
      .getCollectedBy(player)
      .filter { it.isEquipped }
      .map { cosmeticsOfKind[it.cosmeticId.value]!! }

    when (kind) {
      CosmeticKind.Prefix, CosmeticKind.Suffix -> {
        @Suppress("UNCHECKED_CAST")
        val components = (equippedCollected as List<AffixCosmeticDef>).map { it.asNative(attachment.audiences) }
        val component: MutableComponent = Component.empty()
        for (comp in components) {
          if (kind == CosmeticKind.Suffix) component.append(" ")
          component.append(comp)
          if (kind == CosmeticKind.Prefix) component.append(" ")
        }
        cache.getOrDefault(kind, mutableMapOf())[player.uuid] = component

        Main.logger.info("Updated cache for kind $kind: ${cache[kind]}")
        return cache[kind]!!
      }
    }
  }

  fun registerPlaceholders() {
    for (kind in arrayOf(CosmeticKind.Prefix, CosmeticKind.Suffix)) {
      Main.logger.info("Registering placeholders for $kind")

      Placeholders.register(idOf(Main.MOD_ID, "cosmetic_${kind.snakeCaseName()}")) { ctx, _ ->
        if (!ctx.hasPlayer())
          return@register PlaceholderResult.invalid("No player!")
        val player = ctx.player!!

        var cacheForKind = cache[kind]
        if (cacheForKind == null) cacheForKind = updateCacheFor(player, kind)

        var cacheForPlayer = cacheForKind[player.uuid]
        if (cacheForPlayer == null) cacheForPlayer = updateCacheFor(player, kind)[player.uuid]

        return@register PlaceholderResult.value(cacheForPlayer)
      }
    }
  }
}