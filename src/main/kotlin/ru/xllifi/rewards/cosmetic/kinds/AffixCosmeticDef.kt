package ru.xllifi.rewards.cosmetic.kinds

import kotlinx.serialization.Polymorphic
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import ru.xllifi.rewards.config.getServerAttachment
import ru.xllifi.rewards.cosmetic.CosmeticDef
import ru.xllifi.rewards.serializers.text.Component as AdvComponent

@Polymorphic
interface AffixCosmeticDef : CosmeticDef {
  val component: AdvComponent

  fun asNative(audiences: MinecraftServerAudiences): Component = audiences.asNative(component)
  override fun getDisplayName(audiences: MinecraftServerAudiences): Component = asNative(audiences)
}



// TODO: (P1) equipped affixes cache. Change on join and equip toggle.
// TODO: (P2) affixes placeholders, pulling from cache.