package dev.vjcbs.bunqcli

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.round

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)

fun Double.roundTwoDigits() = round(this * 100) / 100
