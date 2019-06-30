package dev.vjcbs.bunqcli

import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.Payment
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

const val contextFile = "bunq.conf"

val bunqDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")!!

data class Summary(
    val incoming: Double = 0.0,
    val outgoing: Double = 0.0
) {
    val delta: Double
        get() = this.incoming + this.outgoing

    operator fun plus(other: Summary) = Summary(
        incoming = this.incoming + other.incoming,
        outgoing = this.outgoing + other.outgoing
    )
}

fun main() {
    // TODO encrypt file

    val apiContext = if (File(contextFile).exists()) {
        ApiContext.restore(contextFile)
    } else {
        ApiContext.create(
            ApiEnvironmentType.PRODUCTION,
            Configuration.bunqApiKey,
            "dev.vjcbs.bunqcli"
        )
    }

    apiContext.save(contextFile)

    BunqContext.loadApiContext(apiContext)

    val summariesPerMonth: MutableMap<String, Summary> = mutableMapOf()

    var page = 0
    var paymentsProcessed = 0
    var nextId: Int? = null

    println()

    do {
        print(".")

        val paymentsResult = Payment.list(Configuration.bunqAccountId, mapOf(
            "count" to "200"
        ) + (nextId?.let {
            mapOf(
                "older_id" to it.toString()
            )
        } ?: mapOf()))

        paymentsProcessed += paymentsResult.value.count()

        paymentsResult.value.filter {
            it.type != "SAVINGS"
        }.map {
            val monthKey = LocalDate.parse(it.created.split(" ")[0], bunqDateTimeFormatter)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val amount = it.amount.value.toDouble()

            monthKey to if (it.subType == "REVERSAL" || amount < 0) {
                Summary(outgoing = amount)
            } else {
                Summary(incoming = amount)
            }
        }.groupBy({ it.first }, { it.second }).map {
            it.key to it.value.fold(Summary()) { acc, curr ->
                acc + curr
            }
        }.forEach {
            summariesPerMonth.merge(it.first, it.second) { a, b ->
                a + b
            }
        }

        nextId = paymentsResult.pagination.olderId
        page++

        if (page % 10 == 0) println(" $page")
    } while (nextId != null)

    println()
    println()

    summariesPerMonth.forEach {
        println("[${it.key}]")
        println("\tIncoming:\t${it.value.incoming.roundTwoDigits()}")
        println("\tOutgoing:\t${it.value.outgoing.roundTwoDigits()}")
        println("\tDelta:\t\t${it.value.delta.roundTwoDigits()}")
        println()
    }

    println("Transactions processed: $paymentsProcessed")
}
