package ru.xllifi.rewards.utils

import ru.xllifi.rewards.serializers.ResourceLocation

fun resLoc(ns: String, path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ns, path)