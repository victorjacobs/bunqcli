package dev.vjcbs.bunqcli

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt

class BunqCli : CliktCommand() {
    override fun run() {}
}

class SummaryCommand : CliktCommand(
    name = "summary",
    help = "Prints out monthly summary of transactions"
) {

    private val password by option().prompt(hideInput = true)

    private val bunqClient = BunqClient()

    override fun run() {
        val configuration = Configuration.fromFileWithPassword(password)

        if (configuration.apiContext == null) {
            val bunqApiKey = TermUi.prompt(
                text = "Bunq API key"
            ) ?: return

            configuration.apiContext = bunqClient.loginWithApiKey(bunqApiKey)
            configuration.save()
        } else {
            configuration.apiContext?.also {
                bunqClient.loginWithContext(it)
            }
        }

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

        val summariesPerMonth = bunqClient.summariesPerMonthWhile(configuration.bunqAccountId!!) {
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

fun main(args: Array<String>) = BunqCli()
    .subcommands(SummaryCommand())
    .main(args)
