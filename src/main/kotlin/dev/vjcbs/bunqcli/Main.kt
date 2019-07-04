package dev.vjcbs.bunqcli

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import java.time.LocalDate

private const val password = "test"

class BunqCli : CliktCommand() {

    private val configuration = Configuration.fromFileWithPassword(password)

    private val bunqClient = BunqClient()

    override fun run() {
        if (configuration.encryptedApiContext == null) {
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
            it.getCreatedDateTime().month == LocalDate.now().month
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

fun main(args: Array<String>) = BunqCli().main(args)
