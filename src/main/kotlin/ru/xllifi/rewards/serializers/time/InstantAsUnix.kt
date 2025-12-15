package ru.xllifi.rewards.serializers.time

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

val unixHumanFormat = DateTimeComponents.Format {
  hour()
  char(':')
  minute()
  char(':')
  second()
  char(' ')
  year()
  char('-')
  monthNumber()
  char('-')
  day()
  chars(" (UTC")
  offsetHours()
  char(')')
}

typealias InstantAsUnix = @Serializable(InstantAsUnixSerializer::class) Instant

fun InstantAsUnix.unixHumanReadable(): String {
  return this.format(unixHumanFormat, TimeZone.currentSystemDefault().offsetAt(this))
}

object InstantAsUnixSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("InstantAsUnix", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeLong(value.epochSeconds)
  }

  override fun deserialize(decoder: Decoder): Instant {
    return Instant.fromEpochSeconds(decoder.decodeLong())
  }
}