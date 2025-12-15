package ru.xllifi.rewards.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation

typealias ResourceLocation = @Serializable(with = ResourceLocationSerializer::class) ResourceLocation

object ResourceLocationSerializer : KSerializer<ResourceLocation> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("resource_location", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ResourceLocation) =
    encoder.encodeString("${value.namespace}:${value.path}")

  override fun deserialize(decoder: Decoder): ResourceLocation {
    val split = decoder.decodeString().split(":")
    if (split.size != 2) {
      throw SerializationException("ResourceLocation should be formatted like \"namespace:path\"!")
    }
    return ResourceLocation.fromNamespaceAndPath(split[0], split[1])
  }
}