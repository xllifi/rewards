package ru.xllifi.rewards.serializers

import com.mojang.serialization.Codec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.server.MinecraftServer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.ItemStack
import ru.xllifi.rewards.config.defaultJson
import kotlin.reflect.KClass

class JsonSerializers(
  server: MinecraftServer,
) {

  private val registryOps = server.registryAccess().createSerializationContext(JsonOpsKotlinx) ?: JsonOpsKotlinx

  inner class CodecSerializer<T: Any>(
    private val codec: Codec<T>,
    kClass: KClass<T>,
  ) : KSerializer<T> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("CodecSerializer(${kClass.qualifiedName})", SerialKind.CONTEXTUAL)

    override fun deserialize(decoder: Decoder): T {
      require(decoder is JsonDecoder)
      val jsonElement = decoder.decodeJsonElement()
      return codec.parse(registryOps, jsonElement).getOrThrow()
    }

    override fun serialize(encoder: Encoder, value: T) {
      require(encoder is JsonEncoder)
      val jsonElement = codec.encodeStart(registryOps, value).getOrThrow()
      encoder.encodeJsonElement(jsonElement)
    }
  }

  private val module = SerializersModule {
    contextual(ItemStack::class, CodecSerializer(
      codec = ItemStack.CODEC,
      kClass = ItemStack::class,
    ))
    contextual(SoundEvent::class, CodecSerializer(
      codec = SoundEvent.DIRECT_CODEC,
      kClass = SoundEvent::class,
    ))
  }

  val json = Json(defaultJson) {
    serializersModule = module
  }

  val jsonStrict get() = Json(json) {
    ignoreUnknownKeys = false
    explicitNulls = true
  }
}
