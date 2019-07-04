package dev.vjcbs.bunqcli

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

data class Configuration(
    var bunqApiContext: AesEncryptionResult? = null,
    @JsonIgnore
    var decryptedApiContext: String? = null
) {

    companion object {

        private val log = logger()

        private val configurationPath = "conf.json"

        private val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule())
        }

        fun fromFileWithPassword(password: String?): Configuration? {
            val configurationFile = File(configurationPath)

            if (!configurationFile.exists()) {
                log.info("Configuration file doesn't exist")
                return null
            }

            log.info("Reading configuration from file")

            val configuration = try {
                objectMapper.readValue(configurationFile, Configuration::class.java)
            } catch (e: Exception) {
                log.error("Deserializing configuration failed", e)
                return null
            }

            if (password != null) {
                configuration.decryptedApiContext = configuration.bunqApiContext?.decrypt(password)
            }

            return configuration
        }
    }

    fun writeToFileWithPassword(password: String) {
        log.info("Writing configuration to file")

        bunqApiContext = decryptedApiContext?.encrypt(password)

        val json = objectMapper.writeValueAsString(this)

        Files.write(Paths.get(configurationPath), json.toByteArray())
    }
}
