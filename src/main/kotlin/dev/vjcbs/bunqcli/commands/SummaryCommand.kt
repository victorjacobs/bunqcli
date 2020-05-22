package dev.vjcbs.bunqcli.commands

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import dev.vjcbs.bunqcli.BunqClient
import dev.vjcbs.bunqcli.Configuration
import dev.vjcbs.bunqcli.roundTwoDigits

class SummaryCommand : CliktCommand(
    name = "summary",
    help = "Prints out monthly summary of transactions"
) {
    private val password by option().prompt(hideInput = true)

    override fun run() {
        val configuration = Configuration.fromFileWithPassword(password)
        BunqClient.login(configuration)

        if (configuration.bunqAccountId == null) {
            println("Bunq account id not set, please choose one of the following:")

            MonetaryAccountBank.list().value.filter { it.status == "ACTIVE" }.forEach {
                println("${it.id}\t${it.description}")
            }

            println()

            val bunqAccountId = TermUi.prompt(
                text = "Account id"
            ) ?: return

            configuration.bunqAccountId = bunqAccountId.toInt()
            configuration.save()
        }

        val summariesPerMonth = BunqClient.summariesPerMonthWhile(configuration.bunqAccountId!!) {
            true
        }

        println()

        summariesPerMonth.forEach {
            println("[${it.first}]")
            println("\tIncoming:\t${formatAmount(it.second.incoming)}")
            println("\tOutgoing:\t${formatAmount(it.second.outgoing)}")
            println("\tDelta:\t\t${formatAmount(it.second.delta)}")
            println()
        }
    }

    private fun formatAmount(amount: Double) = String.format("%8.2f", amount.roundTwoDigits())
}
