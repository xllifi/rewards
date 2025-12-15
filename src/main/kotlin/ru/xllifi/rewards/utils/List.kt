package ru.xllifi.rewards.utils

fun <T> List<T>.resizeEnd(size: Int, padElement: T, foldFunction: (T, T) -> T): List<T> =
  if (this.size <= size) {
    this + List(size - this.size) { padElement }
  } else {
    this.take(size - 1) + this.drop(size - 1).fold(padElement, foldFunction)
  }