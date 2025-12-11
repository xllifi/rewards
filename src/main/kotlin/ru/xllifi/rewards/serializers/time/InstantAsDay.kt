package ru.xllifi.rewards.serializers.time

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

typealias InstantAsDay = @Serializable(InstantAsDaySerializer::class) Instant

object InstantAsDaySerializer : KSerializer<Instant> {
  val format = DateTimeComponents.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char('@')
    char('U')
    char('T')
    char('C')
    offsetHours()
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("InstantAsDay", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.format(format, TimeZone.currentSystemDefault().offsetAt(value)))
  }

  override fun deserialize(decoder: Decoder): Instant {
    val components = format.parse(decoder.decodeString())
    val localDateTime = LocalDateTime(
      year = components.year!!,
      month = components.month!!,
      day = components.day!!,
      hour = 0,
      minute = 0,
      second = 0,
      nanosecond = 0
    )
    return localDateTime.toInstant(UtcOffset(components.offsetHours!!))
  }
}