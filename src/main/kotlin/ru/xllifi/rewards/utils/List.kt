package ru.xllifi.rewards.utils

fun <T> List<T>.resizeEnd(size: Int, padElement: T, foldFunction: (T, T) -> T): List<T> =
  if (this.size <= size) {
    this + List(size - this.size) { padElement }
  } else {
    this.take(size - 1) + this.drop(size - 1).fold(padElement, foldFunction)
  }

fun <T> List<T>.extendAndAlign(desiredSize: Int, padElement: T): List<T> {
  if (desiredSize < this.size)
    throw IllegalArgumentException("Desired size $desiredSize must be greater than List size ${this.size}")
  if (desiredSize % 2 == 0)
    throw IllegalArgumentException("Desired size $desiredSize must be odd!")

  val sizeIsEven = this.size % 2 == 0
  val mutableThis = this.toMutableList()
  if (sizeIsEven) {
    val index = this.size / 2
    mutableThis.add(index, padElement)
  }

  val padSize = desiredSize - mutableThis.size
  val padSizeIsEven = padSize % 2 == 0
  if (!padSizeIsEven) throw IllegalStateException("Pad size $padSize is somehow not even!")
  val halfPadSize = padSize / 2

  return List(halfPadSize) { padElement } + mutableThis + List(halfPadSize) { padElement }
}