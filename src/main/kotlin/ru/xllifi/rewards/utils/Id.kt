package ru.xllifi.rewards.utils

import ru.xllifi.rewards.serializers.Identifier

fun id(ns: String, path: String): Identifier = Identifier.fromNamespaceAndPath(ns, path)