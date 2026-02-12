package ru.xllifi.rewards.utils

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

// String extensions
fun String.camelToSnakeCase(): String {
  return camelRegex.replace(this) {
    "_${it.value}"
  }.lowercase()
}