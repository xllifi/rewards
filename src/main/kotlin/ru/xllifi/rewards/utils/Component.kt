package ru.xllifi.rewards.utils

import net.minecraft.network.chat.MutableComponent
import ru.xllifi.rewards.serializers.text.Component

operator fun Component.plus(other: Component): Component = this.append(other)

operator fun MutableComponent.plus(other: MutableComponent): MutableComponent = this.append(other)