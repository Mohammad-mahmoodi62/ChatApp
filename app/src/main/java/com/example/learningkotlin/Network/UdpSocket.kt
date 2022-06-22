import com.example.learningkotlin.*
import com.jaredrummler.android.device.DeviceName
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.*
import java.util.*
import kotlin.experimental.and


class UdpSocket(portNumber: Int, val handleReceveidMsg: (msg: BaseMessage, dp: DatagramPacket) -> Unit) {

    private lateinit var _socket: DatagramSocket
    private var receiveBool: Boolean = false
    var broadcastIps = mutableListOf<InetAddress>()
    lateinit var selfIP: String


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
        val broadcastMessage = RevealMessage()
        val json = Json { encodeDefaults = true }
        val serialized = json.encodeToString(broadcastMessage as BaseMessage)
        for(bca in broadcastIps)
        {
            val msg = DatagramPacket(serialized.toByteArray(), serialized.length, bca, 3000)
            _socket.send(msg)
        }
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

            this.handleReceveidMsg(deserialized, dp)

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

    fun sendMsg(msg: BaseMessage, dp: DatagramPacket) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            val sendMsg =
                DatagramPacket(serialized.toByteArray(), serialized.length, dp.address, 3000)
            this._socket.send(sendMsg)
        }
        threadPool.run(worker)
    }

    fun sendMsg(msg: BaseMessage, ipAddress: InetAddress) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            val sendMsg =
                DatagramPacket(serialized.toByteArray(), serialized.length, ipAddress, 3000)
            this._socket.send(sendMsg)
        }
        threadPool.run(worker)
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