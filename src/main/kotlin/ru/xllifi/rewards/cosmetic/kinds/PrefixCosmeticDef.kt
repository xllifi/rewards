package ru.xllifi.rewards.cosmetic.kinds

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.xllifi.rewards.cosmetic.CosmeticKind
import ru.xllifi.rewards.serializers.text.Component as AdvComponent

@Serializable
@SerialName("prefix")
class PrefixCosmeticDef(
  override val id: String,
  override val component: AdvComponent,
  override val shouldCountInTotal: Boolean,
) : AffixCosmeticDef {
  @Transient
  override val kind: CosmeticKind = CosmeticKind.Prefix
}