import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement
import javax.crypto.interfaces.DHPublicKey
import javax.crypto.spec.SecretKeySpec

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

    fun aliceGenerateSecretKey(bobPubKeyEnc: ByteArray): SecretKeySpec {
        val aliceKeyFac = KeyFactory.getInstance("DH")
        val bobPubKey = aliceKeyFac.generatePublic(X509EncodedKeySpec(bobPubKeyEnc))
        println("ALICE: Execute PHASE1 ...")
        this._keyAgree.doPhase(bobPubKey, true)

        val aliceSharedSecret = this._keyAgree.generateSecret()
        return SecretKeySpec(aliceSharedSecret, 0, 32, "AES")
    }

    fun bobGenerateSecretKey(): SecretKeySpec{
        this._keyAgree.doPhase(this._receivedPubKey, true)

        val bobSharedSecret = this._keyAgree.generateSecret()
        return SecretKeySpec(bobSharedSecret, 0, 32, "AES")
    }
}