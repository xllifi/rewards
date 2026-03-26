package ru.xllifi.rewards.utils.extensions

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and

operator fun Op<Boolean>.plus(other: Op<Boolean>): Op<Boolean> =
  this.and(other)