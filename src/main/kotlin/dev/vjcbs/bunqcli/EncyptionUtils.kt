package dev.vjcbs.bunqcli

import org.apache.commons.codec.binary.Base64
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private const val keyAlgorithm = "AES"
private const val cipherAlgorithm = "AES/GCM/PKCS5Padding"
private const val ivLength = 16
private const val gcmTagLength = 128

private const val pbkdf2Algorithm = "PBKDF2WithHmacSHA512"
private const val pbkdf2Iterations = 100_000
private const val pbkdf2Strength = 256
private const val saltLength = 16

fun String.encrypt(password: String): AesEncryptionResult? {
    val log = logger()

    try {
        val random = SecureRandom()
        val iv = ByteArray(ivLength)
        random.nextBytes(iv)

        val salt = ByteArray(saltLength)
        random.nextBytes(salt)

        val secretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Algorithm)
        val secretKey = secretKeyFactory.generateSecret(PBEKeySpec(
            password.toCharArray(),
            salt,
            pbkdf2Iterations,
            pbkdf2Strength
        ))

        val cipher = Cipher.getInstance(cipherAlgorithm)
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(secretKey.encoded, keyAlgorithm),
            GCMParameterSpec(gcmTagLength, iv)
        )

        val encryptedData = cipher.doFinal(this.toByteArray())

        return AesEncryptionResult(iv, salt, encryptedData)
    } catch (e: GeneralSecurityException) {
        log.error("Encryption failed", e)
        return null
    }
}

data class AesEncryptionResult(
    val iv: String,
    val salt: String,
    val data: String
) {
    constructor(ivBytes: ByteArray, saltBytes: ByteArray, dataBytes: ByteArray) :
            this(
                iv = Base64.encodeBase64String(ivBytes),
                salt = Base64.encodeBase64String(saltBytes),
                data = Base64.encodeBase64String(dataBytes)
            )

    private val log = logger()

    private fun ivBytes() = Base64.decodeBase64(iv)!!

    private fun saltBytes() = Base64.decodeBase64(salt)!!

    private fun dataBytes() = Base64.decodeBase64(data)!!

    fun decrypt(password: String): String? {
        try {
            val secretKeyFactory = SecretKeyFactory.getInstance(pbkdf2Algorithm)
            val secretKey = secretKeyFactory.generateSecret(PBEKeySpec(
                password.toCharArray(),
                saltBytes(),
                pbkdf2Iterations,
                pbkdf2Strength
            ))

            val cipher = Cipher.getInstance(cipherAlgorithm)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(secretKey.encoded, keyAlgorithm),
                GCMParameterSpec(gcmTagLength, ivBytes())
            )

            val decryptedData = cipher.doFinal(dataBytes())

            return String(decryptedData)
        } catch (e: GeneralSecurityException) {
            log.error("Decryption failed: ${e.message}")
            return null
        }
    }
}
