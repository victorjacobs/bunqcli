package dev.vjcbs.bunqcli

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

data class Configuration(
    @JsonIgnore
    private var password: String? = null,

    var encryptedApiContext: AesEncryptionResult? = null,
    @JsonIgnore
    var apiContext: String? = null,
    var bunqAccountId: Int? = null
) {

    companion object {

        private val configurationPath =
            Paths.get(System.getProperty("user.home"), ".bunqclirc")

        private val log = logger()

        private val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule())
        }

        fun fromFileWithPassword(password: String?): Configuration {
            val configuration = Configuration(
                password = password
            )

            if (!Files.exists(configurationPath)) {
                log.info("Configuration file doesn't exist")
                return configuration
            }

            log.info("Reading configuration from file")

            try {
                objectMapper.readerForUpdating(configuration).readValue<Configuration>(configurationPath.toFile())
            } catch (e: Exception) {
                log.error("Deserializing configuration failed", e)
                return configuration
            }

            if (password != null) {
                val decryptionTime = measureTimeMillis {
                    configuration.apiContext = configuration.encryptedApiContext?.decrypt(password)
                }

                log.debug("Decryption took ${decryptionTime}ms")
            }

            return configuration
        }
    }

    fun save() {
        log.info("Writing configuration to file")

        password?.also {
            val encryptionTime = measureTimeMillis {
                encryptedApiContext = apiContext?.encrypt(it)
            }

            log.debug("Encryption took ${encryptionTime}ms")
        }

        val json = objectMapper.writeValueAsString(this)

        Files.write(configurationPath, json.toByteArray())
    }
}
