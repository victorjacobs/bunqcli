package dev.vjcbs.bunqcli

import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.Payment
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BunqClient {

    private val contextFile = "bunq.conf"

    init {
        login()
    }

    fun login() {
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
    }

    fun getMostRecentPayments(bunqAccountId: Int) = getMostRecentPaymentsWhile(bunqAccountId) { true }

    fun getMostRecentPaymentsWhile(bunqAccountId: Int, condition: (Payment) -> Boolean): List<Payment> {
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

        return result
    }
}

fun Payment.getCreatedDateTime(): LocalDate {
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")!!

    return LocalDate.parse(created.split(" ")[0], dateTimeFormatter)
}

fun Payment.getAmountDouble(): Double = amount.value.toDouble()
