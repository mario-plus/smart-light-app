package com.unilumin.smartapp.util

import android.os.Build
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

class EncryptUtil {

    fun encryptPass(content: String, base64PublicKey: String?): String? {
        val publicKey = getPublicKey(base64PublicKey)
        return encryptData(content, publicKey)
    }


    private fun getPublicKey(base64PublicKey: String?): PublicKey? {
        try {
            var publicKeyData: ByteArray? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                publicKeyData = Base64.getDecoder().decode(base64PublicKey)
            }
            val spec = X509EncodedKeySpec(publicKeyData)
            val keyFactory = KeyFactory.getInstance("RSA")
            return keyFactory.generatePublic(spec)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun encryptData(plainText: String, publicKey: PublicKey?): String? {
        try {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding") // 注意转换模式
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val bytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(bytes)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return null
    }
}