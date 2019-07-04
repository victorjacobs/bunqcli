package dev.vjcbs.bunqcli

import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.Payment
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BunqClient {

    private val password = "test"

    init {
        login()
    }

    fun login() {
        val apiContext = Configuration.fromFileWithPassword(password)?.let {
            ApiContext.fromJson(it.decryptedApiContext)
        } ?: run {
            val context = ApiContext.create(
                ApiEnvironmentType.PRODUCTION,
                EnvironmentVariables.bunqApiKey,
                "dev.vjcbs.bunqcli"
            )

            val conf = Configuration(decryptedApiContext = context.toJson())
            conf.writeToFileWithPassword(password)

            context
        }

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

fun Payment.getCreatedDateTime() =
    LocalDate.parse(created.split(" ")[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))!!

fun Payment.getAmountDouble() = amount.value.toDouble()
