package dev.vjcbs.bunqcli

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import java.time.LocalDate

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
            println("\tIncoming:\t${it.second.incoming.roundTwoDigits()}")
            println("\tOutgoing:\t${it.second.outgoing.roundTwoDigits()}")
            println("\tDelta:\t\t${it.second.delta.roundTwoDigits()}")
            println()
        }
    }
}

fun main(args: Array<String>) = BunqCli()
    .subcommands(SummaryCommand())
    .main(args)
