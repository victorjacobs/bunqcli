package dev.vjcbs.bunqcli

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

data class Configuration(
    @JsonIgnore
    private var password: String? = null,

    var encryptedApiContext: AesEncryptionResult? = null,
    @JsonIgnore
    var apiContext: String? = null,
    var bunqAccountId: Int? = null
) {

    companion object {

        private const val configurationPath = "conf.json"

        private val log = logger()

        private val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule())
        }

        fun fromFileWithPassword(password: String?): Configuration {
            val configurationFile = File(configurationPath)

            if (!configurationFile.exists()) {
                log.info("Configuration file doesn't exist")
                return Configuration()
            }

            log.info("Reading configuration from file")

            val configuration = try {
                objectMapper.readValue(configurationFile, Configuration::class.java).apply {
                    this.password = password
                }
            } catch (e: Exception) {
                log.error("Deserializing configuration failed", e)
                return Configuration()
            }

            if (password != null) {
                configuration.apiContext = configuration.encryptedApiContext?.decrypt(password)
            }

            return configuration
        }
    }

    fun save() {
        log.info("Writing configuration to file")

        password?.also {
            encryptedApiContext = apiContext?.encrypt(it)
        }

        val json = objectMapper.writeValueAsString(this)

        Files.write(Paths.get(configurationPath), json.toByteArray())
    }
}
