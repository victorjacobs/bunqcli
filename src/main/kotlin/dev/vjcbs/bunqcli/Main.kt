package dev.vjcbs.bunqcli

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val bunqClient = BunqClient()

fun main() {
    if (Configuration.bunqAccountId == null) {
        println("BUNQ_ACCOUNT_ID not set, please choose one of the following:")

        MonetaryAccountBank.list().value.filter { it.status == "ACTIVE" }.forEach {
            println("${it.id}\t${it.description}")
        }

        return
    }

    val payments = bunqClient.getMostRecentPayments(Configuration.bunqAccountId)

    val summariesPerMonth = payments.filter {
        it.type != "SAVINGS"
    }.map {
        val monthKey = it.getCreatedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val amount = it.getAmountDouble()

        monthKey to if (it.subType == "REVERSAL" || amount < 0) {
            Summary(outgoing = amount)
        } else {
            Summary(incoming = amount)
        }
    }.groupBy({ it.first }, { it.second }).map {
        it.key to it.value.fold(Summary()) { acc, curr ->
            acc + curr
        }
    }

    println()

    summariesPerMonth.forEach {
        println("[${it.first}]")
        println("\tIncoming:\t${it.second.incoming.roundTwoDigits()}")
        println("\tOutgoing:\t${it.second.outgoing.roundTwoDigits()}")
        println("\tDelta:\t\t${it.second.delta.roundTwoDigits()}")
        println()
    }

    println("Transactions processed: ${payments.count()}")
}
