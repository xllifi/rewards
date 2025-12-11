package ru.xllifi.rewards.utils

import ru.xllifi.rewards.serializers.text.Component

operator fun Component.plus(other: Component): Component = this.append(other)