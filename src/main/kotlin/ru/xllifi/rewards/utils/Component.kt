package ru.xllifi.rewards.utils

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

operator fun Component.plus(other: Component): Component = this.copy().append(other)

operator fun MutableComponent.plus(other: MutableComponent): MutableComponent = this.append(other)