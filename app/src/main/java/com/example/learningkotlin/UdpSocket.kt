import com.example.learningkotlin.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.*



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


    init {
        val worker = Runnable {
            this._socket = DatagramSocket(portNumber)
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
        val broadcastMessage = revealMessage()
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
            "request-number" -> this.handleRequesterNumber(dp, msg as requesterNumber)
            "response-number" -> {
                this.receivedRandomNumber = (msg as responserNumber).num
                this.cHandler.otherId = this.receivedRandomNumber
                this.decideWhoIsServer(dp.address)
            }
            "server-ready" -> {
                this.cHandler.addClient(dp.address)
                this.cHandler.addUserToViewModel(this.receivedRandomNumber)
            }
        }
    }

    private fun handleRequesterNumber(dp: DatagramPacket, rn: requesterNumber) {
        this.receivedRandomNumber = rn.num
        this.cHandler.otherId = this.receivedRandomNumber

        val randomNumber = responserNumber((0..100).random())
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

        val randomNumber = requesterNumber((0..100).random())
        this.selfRandomNumber = randomNumber.num
        this.cHandler.setSelfId(this.selfRandomNumber)
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(randomNumber as BaseMessage)

        val msg = DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
        _socket.send(msg)

    }

    private fun decideWhoIsServer(ipAddress: InetAddress) {
        BugRepoter.log("inside decideWhoIsServer")

        if (this.receivedRandomNumber > this.selfRandomNumber)
        {
            // this phone is client
        }
        else
        {
            // this phone is server
            this.cHandler.startServer()
            this.cHandler.addUserToViewModel(this.receivedRandomNumber)
            val readyMsg = ServerIsReady()
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(readyMsg as BaseMessage)

            val msg = DatagramPacket(serialized.toByteArray(), serialized.length, ipAddress, 3000)
            _socket.send(msg)
        }

    }

    private fun identifySelf(dp:DatagramPacket) {
        revealRequestUsers.add(dp.address)
        val broadcastMessage = hereIAm()
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