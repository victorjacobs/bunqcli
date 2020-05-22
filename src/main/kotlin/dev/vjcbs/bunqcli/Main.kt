package dev.vjcbs.bunqcli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import dev.vjcbs.bunqcli.commands.ListAccountsCommand
import dev.vjcbs.bunqcli.commands.SummaryCommand

class BunqCli : CliktCommand() {
    override fun run() {}
}

fun main(args: Array<String>) = BunqCli()
    .subcommands(SummaryCommand())
    .subcommands(ListAccountsCommand())
    .main(args)
