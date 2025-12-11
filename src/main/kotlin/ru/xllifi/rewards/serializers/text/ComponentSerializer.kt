package ru.xllifi.rewards.serializers.text

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

typealias Component = @Serializable(with = ComponentSerializer::class) Component

object ComponentSerializer : KSerializer<Component> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("component", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Component) =
    encoder.encodeString(MiniMessage.miniMessage().serialize(value))

  override fun deserialize(decoder: Decoder): Component =
    MiniMessage.miniMessage().deserialize(decoder.decodeString())
}