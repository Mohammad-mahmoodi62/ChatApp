import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class KeyAgreement {
    private lateinit var _keyAgree: KeyAgreement
    private lateinit var _receivedPubKey: PublicKey

    fun generateAlicePublicKey(): ByteArray {
        val aliceKpairGen = KeyPairGenerator.getInstance("DH")
        aliceKpairGen.initialize(512)
        val aliceKpair = aliceKpairGen.generateKeyPair()

        println("ALICE: Initialization ...")
        this._keyAgree = KeyAgreement.getInstance("DH")
        this._keyAgree.init(aliceKpair.private)

        val alicePubKeyEnc = aliceKpair.public.encoded
        return alicePubKeyEnc
    }

    fun generateBobPublicKey(alicePubKeyEnc: ByteArray): ByteArray {
        val bobKeyFac = KeyFactory.getInstance("DH")
        this._receivedPubKey = bobKeyFac.generatePublic(X509EncodedKeySpec(alicePubKeyEnc))

        val dhParamFromAlicePubKey = (this._receivedPubKey as DHPublicKey).params

        val bobKpairGen = KeyPairGenerator.getInstance("DH")
        bobKpairGen.initialize(dhParamFromAlicePubKey)
        val bobKpair = bobKpairGen.generateKeyPair()

        this._keyAgree = KeyAgreement.getInstance("DH")
        this._keyAgree.init(bobKpair.private)

        val bobPubKeyEnc = bobKpair.public.encoded
        return bobPubKeyEnc
    }

    fun aliceGenerateNeededKeys(bobPubKeyEnc: ByteArray): Triple<SecretKey, SecretKey, ByteArray> {
        val aliceKeyFac = KeyFactory.getInstance("DH")
        val bobPubKey = aliceKeyFac.generatePublic(X509EncodedKeySpec(bobPubKeyEnc))
        println("ALICE: Execute PHASE1 ...")
        this._keyAgree.doPhase(bobPubKey, true)

        val aliceSharedSecret = this._keyAgree.generateSecret()
        val encryptionKey = SecretKeySpec(aliceSharedSecret, 0, 32, "AES")
        val macKey = SecretKeySpec(aliceSharedSecret, 31, 32, "AES")
        val iv = ByteArray(16) { i -> aliceSharedSecret[i+5].xor(aliceSharedSecret[16 + i+5]) }
        return Triple(encryptionKey, macKey, iv)
    }

    fun bobGenerateNeededKeys(): Triple<SecretKey, SecretKey, ByteArray>{
        this._keyAgree.doPhase(this._receivedPubKey, true)

        val bobSharedSecret = this._keyAgree.generateSecret()
        val encryptionKey = SecretKeySpec(bobSharedSecret, 0, 32, "AES")
        val macKey = SecretKeySpec(bobSharedSecret, 31, 32, "AES")
        val iv = ByteArray(16) { i -> bobSharedSecret[i+5].xor(bobSharedSecret[16 + i+5]) }
        return Triple(encryptionKey, macKey, iv)
    }
}