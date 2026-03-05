package ru.xllifi.rewards.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.Identifier

typealias Identifier = @Serializable(with = IdentifierSerializer::class) Identifier

object IdentifierSerializer : KSerializer<ru.xllifi.rewards.serializers.Identifier> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ru.xllifi.rewards.serializers.Identifier) =
    encoder.encodeString("${value.namespace}:${value.path}")

  override fun deserialize(decoder: Decoder): ru.xllifi.rewards.serializers.Identifier {
    val split = decoder.decodeString().split(":")
    if (split.size != 2) {
      throw SerializationException("Identifier should be formatted like \"namespace:path\"!")
    }
    return Identifier.fromNamespaceAndPath(split[0], split[1])
  }
}