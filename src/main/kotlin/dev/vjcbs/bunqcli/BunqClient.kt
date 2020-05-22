package dev.vjcbs.bunqcli

import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.Payment
import com.github.ajalt.clikt.output.TermUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object BunqClient {
    private val log = logger()

    fun login(configuration: Configuration) {
        if (configuration.apiContext == null) {
            val bunqApiKey = TermUi.prompt(
                text = "Bunq API key"
            ) ?: throw Error("Empty API key")

            log.debug("Logging in with api key")
            val context = ApiContext.create(
                ApiEnvironmentType.PRODUCTION,
                bunqApiKey,
                "dev.vjcbs.bunqcli"
            )
            BunqContext.loadApiContext(context)
            configuration.apiContext = context.toJson()
            configuration.save()
        } else {
            configuration.apiContext?.also {
                log.debug("Logging in with context")
                BunqContext.loadApiContext(ApiContext.fromJson(it))
            }
        }
    }

    fun mostRecentPaymentsWhile(bunqAccountId: Int, condition: (Payment) -> Boolean): List<Payment> {
        val result: MutableList<Payment> = mutableListOf()

        var nextId: Int? = null

        do {
            val paymentsResult = Payment.list(bunqAccountId, mapOf(
                "count" to "200"
            ) + (nextId?.let {
                mapOf(
                    "older_id" to it.toString()
                )
            } ?: mapOf()))

            val filteredPayments = paymentsResult.value.takeWhile(condition)
            result.addAll(filteredPayments)

            nextId = paymentsResult.pagination.olderId
        } while (nextId != null && filteredPayments.count() == paymentsResult.value.count())

        log.info("Fetched ${result.size} payments")

        return result
    }

    fun summariesPerMonthWhile(bunqAccountId: Int, condition: (Payment) -> Boolean) =
        mostRecentPaymentsWhile(bunqAccountId, condition).filter {
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

}

fun Payment.getCreatedDateTime() =
    LocalDate.parse(created.split(" ")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))!!

fun Payment.getAmountDouble() = amount.value.toDouble()
