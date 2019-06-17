package dev.vjcbs.bunqcli

object Configuration {

    val bunqAccountId = getFromEnvOrThrow("BUNQ_ACCOUNT_ID").toInt()

    val bunqApiKey = getFromEnvOrThrow("BUNQ_API_KEY")

    private fun getFromEnv(varName: String): String? = System.getenv(varName)

    private fun getFromEnvOrThrow(varName: String) =
        getFromEnv(varName) ?: throw IllegalStateException("$varName not set")

}
