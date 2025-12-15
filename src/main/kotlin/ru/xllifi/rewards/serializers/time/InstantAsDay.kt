package ru.xllifi.rewards.serializers.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
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
import ru.xllifi.rewards.Main
import kotlin.time.Instant

val dayHumanFormat = DateTimeComponents.Format {
  year()
  char('-')
  monthNumber()
  char('-')
  day()
  chars(" (UTC")
  offsetHours()
  char(')')
}

typealias InstantAsDay = @Serializable(InstantAsDaySerializer::class) Instant

fun InstantAsDay.dayHumanReadable(): String =
  this.format(dayHumanFormat, Main.globalConfig.timeZoneForSure.offsetAt(this))

object InstantAsDaySerializer : KSerializer<Instant> {
  val format = DateTimeComponents.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("InstantAsDay", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeString(value.format(format, Main.globalConfig.timeZoneForSure.offsetAt(value)))
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
    return localDateTime.toInstant(Main.globalConfig.timeZoneForSure)
  }
}