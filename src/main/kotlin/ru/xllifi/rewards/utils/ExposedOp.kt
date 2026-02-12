package ru.xllifi.rewards.utils

import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and

operator fun Op<Boolean>.plus(other: Op<Boolean>): Op<Boolean> =
  this.and(other)

fun Op.Companion.all(firstOp: Op<Boolean>, vararg ops: Op<Boolean>): Op<Boolean> =
  firstOp + ops.reduce { acc, op -> acc + op }