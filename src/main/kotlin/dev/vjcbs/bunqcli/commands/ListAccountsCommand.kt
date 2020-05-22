package dev.vjcbs.bunqcli.commands

import com.bunq.sdk.model.generated.endpoint.MonetaryAccountBank
import com.bunq.sdk.model.generated.endpoint.MonetaryAccountJoint
import com.bunq.sdk.model.generated.endpoint.MonetaryAccountSavings
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import dev.vjcbs.bunqcli.BunqClient
import dev.vjcbs.bunqcli.Configuration

class ListAccountsCommand : CliktCommand(
    name = "list-accounts",
    help = "Lists accounts"
) {
    private val password by option().prompt(hideInput = true)

    override fun run() {
        val configuration = Configuration.fromFileWithPassword(password)
        BunqClient.login(configuration)

        println("Joint accounts:")
        MonetaryAccountJoint.list().value.filter { it.status == "ACTIVE" }.forEach {
            println("${it.id}\t${it.description}")
        }
        println()

        println("Bank accounts:")
        MonetaryAccountBank.list().value.filter { it.status == "ACTIVE" }.forEach {
            println("${it.id}\t${it.description}")
        }
        println()

        println("Saving accounts:")
        MonetaryAccountSavings.list().value.filter { it.status == "ACTIVE" }.forEach {
            println("${it.id}\t${it.description}")
        }
        println()
    }
}
