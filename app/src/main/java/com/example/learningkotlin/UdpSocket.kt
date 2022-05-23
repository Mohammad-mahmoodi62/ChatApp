import com.example.learningkotlin.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.*
import kotlin.experimental.and


class UdpSocket(portNumber: Int) {

    private lateinit var _socket: DatagramSocket
    private var receiveBool: Boolean = false
    var broadcastIps = mutableListOf<InetAddress>()
    private var revealRequestUsers = mutableListOf<InetAddress>()
    private var revealResponseUsers = mutableListOf<InetAddress>()
    private var selfRandomNumber: Int = 0
    private var receivedRandomNumber: Int = 0
    lateinit var cHandler: ConnectionHandler
    private lateinit var selfIP: String
    private lateinit var _keyAgreement: KeyAgreement


    init {
        val worker = Runnable {
            this._socket = DatagramSocket(portNumber)
            this._keyAgreement = KeyAgreement()
            this.fillBroadcastIpAddresses()
            this.receiveBool = true
            this.selfIP = this.getIpv4HostAddress()!!
            val worker = Runnable {
                this.receiver()
            }
            threadPool.run(worker)
        }
        threadPool.run(worker)
    }

    fun findUsers() {
        val broadcastMessage = RevealMessage()
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(broadcastMessage as BaseMessage)
        for(bca in broadcastIps)
        {
            val msg = DatagramPacket(serialized.toByteArray(), serialized.length, bca, 3000)
            _socket.send(msg)
        }
    }

    fun setConnectionHandler(ch: ConnectionHandler) {
        this.cHandler = ch
    }


    private fun fillBroadcastIpAddresses() {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback) continue  // Do not want to use the loopback interface.
            for (interfaceAddress in networkInterface.interfaceAddresses) {
                val broadcast = interfaceAddress.broadcast ?: continue

                this.broadcastIps.add(broadcast)
            }
        }
    }

    private fun receiver() {
        while(receiveBool) {
            val buf = ByteArray(1024)
            val dp = DatagramPacket(buf, 1024)
            _socket.receive(dp)
            if(InetAddress.getByName(this.selfIP) == dp.address)
                continue
            val str = String(dp.data, 0, dp.length)

            lateinit var deserialized: BaseMessage

            try {
                val json = Json { encodeDefaults = true }
                deserialized = json.decodeFromString<BaseMessage>(str)
            }

            catch (e: kotlinx.serialization.SerializationException) {
                //TODO: change print to logcat
                println("error in deserialization ${e.message}")
                continue
            }

            handleReceivedMsg(deserialized, dp)
        }
    }

    private fun getIpv4HostAddress(): String? {
        NetworkInterface.getNetworkInterfaces()?.toList()?.map { networkInterface ->
            networkInterface.inetAddresses?.toList()?.find {
                !it.isLoopbackAddress && it is Inet4Address
            }?.let { return it.hostAddress }
        }
        return ""
    }

    private fun handleReceivedMsg(msg:BaseMessage, dp: DatagramPacket) {
        BugRepoter.log("message type ${msg.javaClass.getAnnotation(SerialName::class.java).value} received")
        when (msg.javaClass.getAnnotation(SerialName::class.java).value) {
            "identify-your-self-msg" -> this.identifySelf(dp)
            "identification-msg" -> this.handleIdentificationMsg(dp)
            "request-number" -> this.handleRequesterNumber(dp, msg as RequesterNumber)
            "response-number" -> {
                this.receivedRandomNumber = (msg as ResponderNumber).num
                this.cHandler.otherId = this.receivedRandomNumber
                this.decideWhoIsServer(dp.address)
            }
            "alice-public-key" -> {
                this.handleAlicePubKey(dp, msg as AlicePubKey)
            }
            "bob-public-key" -> {
                this.handleBobPubKey(msg as BobPubKey, dp)
            }
        }
    }

    private fun handleBobPubKey(bpk: BobPubKey, dp: DatagramPacket) {
        val keys = this._keyAgreement.aliceGenerateNeededKeys(bpk.pubKey)
        this.cHandler.addClient(dp.address, keys)
    }

    private fun handleAlicePubKey(dp: DatagramPacket, apk: AlicePubKey) {
        val msg = BobPubKey(this._keyAgreement.generateBobPublicKey(apk.pubKey))
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(msg as BaseMessage)

        val sendMsg = DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
        _socket.send(sendMsg)

        val keys = this._keyAgreement.bobGenerateNeededKeys()
        ClientHandler.clientKeys[dp.address.toString()] = keys

    }

    private fun handleRequesterNumber(dp: DatagramPacket, rn: RequesterNumber) {
        this.receivedRandomNumber = rn.num
        this.cHandler.otherId = this.receivedRandomNumber

        val randomNumber = ResponderNumber((0..100).random())
        this.selfRandomNumber = randomNumber.num
        this.cHandler.setSelfId(this.selfRandomNumber)
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(randomNumber as BaseMessage)

        val msg = DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
        _socket.send(msg)

        this.decideWhoIsServer(dp.address)
    }

    private fun handleIdentificationMsg(dp: DatagramPacket) {
        revealResponseUsers.add(dp.address)

        val randomNumber = RequesterNumber((0..100).random())
        this.selfRandomNumber = randomNumber.num
        this.cHandler.setSelfId(this.selfRandomNumber)
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(randomNumber as BaseMessage)

        val msg = DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
        _socket.send(msg)

    }

    private fun decideWhoIsServer(ipAddress: InetAddress) {
        BugRepoter.log("inside decideWhoIsServer")

        if (this.receivedRandomNumber > this.selfRandomNumber) {
            //this.cHandler.addClient(ipAddress)
            BugRepoter.log("I am client")
            this.sendAlicePubKey(ipAddress)
        }
        else{
            BugRepoter.log("I am server")
        }

    }

    private fun sendAlicePubKey(ipAddress: InetAddress) {
        val msg = AlicePubKey(this._keyAgreement.generateAlicePublicKey())
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(msg as BaseMessage)

        val sendMsg = DatagramPacket(serialized.toByteArray(), serialized.length, ipAddress, 3000)
        _socket.send(sendMsg)
    }

    private fun identifySelf(dp:DatagramPacket) {
        revealRequestUsers.add(dp.address)
        val broadcastMessage = HereIAm()
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(broadcastMessage as BaseMessage)

        val msg = DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
        _socket.send(msg)
    }

    fun sendMsg(msg: BaseMessage){
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            //TODO: get port from the other user
            val sendMsg = DatagramPacket(serialized.toByteArray(), serialized.length, revealResponseUsers[0], 3000)
            _socket.send(sendMsg)
        }
        threadPool.run(worker)
    }

    fun sendServerRdyMsg() {

    }

    fun destroyObject(){
        this._socket.close()
        this.receiveBool = false
    }
}


fun byte2hex(b: Byte, buf: StringBuffer) {
    val hexChars = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )
    val high: Int = (b.toInt() and 0xf0) shr 4
    val low: Byte = b and 0x0f
    buf.append(hexChars[high])
    buf.append(hexChars[low.toInt()])
}

fun toHexString(block: ByteArray): String {
    val buf = StringBuffer()
    val len = block.size
    for (i in 0 until len) {
        byte2hex(block[i], buf)
        if (i < len - 1) {
            buf.append(":")
        }
    }
    return buf.toString()
}