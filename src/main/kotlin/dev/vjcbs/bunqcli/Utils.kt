package dev.vjcbs.bunqcli

import kotlin.math.round
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)

fun Double.roundTwoDigits() = round(this * 100) / 100
