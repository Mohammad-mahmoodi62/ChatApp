import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class Security(val _encryptionKey: SecretKey, val _macKey:SecretKey, val _iv: ByteArray) {
    fun encrypt(msg: String) : ByteArray {
        val ivspec = IvParameterSpec(this._iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, this._encryptionKey, ivspec)
        val cleartext = msg.toByteArray()
        return cipher.doFinal(cleartext)
    }

    fun decrypt(msg: ByteArray) : String {
        val ivspec = IvParameterSpec(this._iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, this._encryptionKey, ivspec)
        return String(cipher.doFinal(msg))
    }

    fun addHmacSha256(msg: String):ByteArray {
        val sha512Hmac = Mac.getInstance("HmacSHA256")
        sha512Hmac.init(this._macKey)
        val macData: ByteArray = sha512Hmac.doFinal(msg.toByteArray(StandardCharsets.UTF_8))

        println("macData = $macData size = ${macData.size}")
        return msg.toByteArray() + macData
    }

    fun addHmacSha256(msg: ByteArray):ByteArray {
        val sha512Hmac = Mac.getInstance("HmacSHA256")
        sha512Hmac.init(this._macKey)
        val macData: ByteArray = sha512Hmac.doFinal(msg)

        return msg + macData
    }

    fun verifyHmac256(msg: ByteArray): Boolean {
        val mac  = msg.copyOfRange(msg.size - 32/*sha256 size*/, msg.size)
        val pureMsg = msg.copyOf(msg.size - 32/*sha256 size*/)

        val sha512Hmac = Mac.getInstance("HmacSHA256")
        sha512Hmac.init(this._macKey)
        val macData: ByteArray = sha512Hmac.doFinal(pureMsg)

        return mac.contentEquals(macData)
    }

    fun encryptAddHmac(msg: String):ByteArray {
        val encryptData = this.encrypt(msg)
        return this.addHmacSha256(encryptData)
    }

    fun extractMsgFromHmac(msg: ByteArray): ByteArray {
        if (this.verifyHmac256(msg))
            return msg.copyOf(msg.size - 32)
        else
            throw Exception("hmac doesn't match")
    }
}

