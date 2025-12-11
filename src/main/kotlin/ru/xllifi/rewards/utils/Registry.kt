package ru.xllifi.rewards.utils

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

fun <V> Registry<V>.getOrThrow(
  resourceLocation: ResourceLocation
): V {
  val value = try {
    this.get(resourceLocation).get().value()
  } catch (e: Exception) {
    when (e) {
      is NoSuchElementException,
      is IllegalStateException -> {
        throw kotlin.NoSuchElementException(
          "Value for $resourceLocation not found in registry $this!"
        )
      }

      else -> throw e
    }
  }
  return value
}