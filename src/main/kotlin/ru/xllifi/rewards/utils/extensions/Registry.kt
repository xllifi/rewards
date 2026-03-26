package ru.xllifi.rewards.utils.extensions

import net.minecraft.core.Registry
import net.minecraft.resources.Identifier

fun <V : Any> Registry<V>.getOrThrow(
  identifier: Identifier
): V {
  val value = try {
    this.get(identifier).get().value()
  } catch (e: Exception) {
    when (e) {
      is NoSuchElementException,
      is IllegalStateException -> {
        throw kotlin.NoSuchElementException(
          "Value for $identifier not found in registry $this!"
        )
      }

      else -> throw e
    }
  }
  return value
}