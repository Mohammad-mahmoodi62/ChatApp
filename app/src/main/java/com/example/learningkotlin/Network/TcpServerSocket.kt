import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.learningkotlin.*
import com.jaredrummler.android.device.DeviceName
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.*
import java.util.*
import javax.crypto.SecretKey
import kotlin.concurrent.thread

class TcpServerSocket (portNumber: Int, val addToView: (user: UserInfo) -> Unit) {
    private lateinit var _socket: ServerSocket

    init {
        //TODO: make it work for every client
        val worker = Runnable {
            this._socket = ServerSocket(portNumber)
            this.startServer()
        }
        threadPool.run(worker)
    }

    fun startServer() {
        val worker = Runnable {
            try {
                while (!this._socket.isClosed) {
                    val client = this._socket.accept()
                    BugRepoter.log("new client has connected address:" +
                            " ${client.localSocketAddress}")
                    ClientHandler(client, this.addToView)
                }
            }
            catch (e: IOException) {
                this._socket.close()
                BugRepoter.log(e.stackTraceToString())
            }
        }
        threadPool.run(worker)
    }

    fun destroyObject() {
        this._socket.close()
    }
}


class ClientHandler (private val _socket: Socket, val addToView: (user: UserInfo) -> Unit) {
    companion object {
        var clientHandlers = mutableMapOf<String, ClientHandler>()
        var clientKeys = mutableMapOf<String, Triple<SecretKey, SecretKey, ByteArray>>()
    }
    //TODO: also test with BufferReader and BufferWriter
    private lateinit var _outputStream: DataOutputStream
    private lateinit var _inputStream: DataInputStream
    private lateinit var _keys: Triple<SecretKey, SecretKey, ByteArray>
    private lateinit var _security: Security

    init {
        try {
            this._outputStream = DataOutputStream(_socket.getOutputStream())
            this._inputStream = DataInputStream(_socket.getInputStream())
            ClientHandler.clientHandlers[this._socket.inetAddress.toString()] = this
            this.pickupKeys()
            this._security = Security(this._keys.first, this._keys.second, this._keys.third)

            //TODO: change it to nonblocking operations
            Thread(
                Runnable {
                    this.receive()
                }).start()
        }
        catch (e: IOException) {
            BugRepoter.log("can't create input or output stream ${e.message}")
            closeEveryThing()
        }
    }

    private fun pickupKeys() {
        while (true)
        {
            if(ClientHandler.clientKeys.contains(this._socket.inetAddress.toString()))
            {
                this._keys = ClientHandler.clientKeys[this._socket.inetAddress.toString()]!!
                ClientHandler.clientKeys.remove(this._socket.inetAddress.toString())
                break
            }
        }
    }

    private fun receive() {
        var received = ByteArray(8192)
        while (this._socket.isConnected) {
            lateinit var deserialized: BaseMessage
            try {
                val bytesRead = this._inputStream.read(received)
                val message = received.copyOf(bytesRead)

                val encryptedMsg = this._security
                    .extractMsgFromHmac(message)
                val decryptedMsg = this._security.decrypt(encryptedMsg)

                val json = Json { encodeDefaults = true }
                deserialized = json.decodeFromString<BaseMessage>(decryptedMsg)
            }
            catch (e: kotlinx.serialization.SerializationException) {
                BugRepoter.log("error in deserialization ${e.message}")
                continue
            }
            catch (e: java.io.EOFException) {
                BugRepoter.log("client disconnected ${e.message}")
                closeEveryThing()
                break
            }
            catch (e: Exception) {
                BugRepoter.log("error occurred in receiving message ${e.message}")
                continue
            }

            this.handleReceivedMsg(deserialized)
        }
    }

    fun sendMsg(msg: BaseMessage) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            //TODO:change it to writeUTF()
            val macEncrypted = this._security.encryptAddHmac(serialized)
            _outputStream.write(macEncrypted, 0, macEncrypted.size)
        }
        threadPool.run(worker)
    }

    private fun removeFromClientHandler() {
        ClientHandler.clientHandlers.remove(this._socket.localAddress.toString())
        this._inputStream.close()
        this._outputStream.close()
        this._socket.close()
    }

    private fun closeEveryThing() {
        this.removeFromClientHandler()
    }

    private fun handleReceivedMsg(msg:BaseMessage) {

        when(msg.javaClass.getAnnotation(SerialName::class.java).value) {
//            "sam-service" -> this.activity.runOnUiThread{
//                this.textview.text = (msg as OwnedProject2).msg
//            }
//            "chat-message" -> this.cHandler?.addChatToViewModel(msg as ChatMessage)
            "greetings-server" -> {
                (msg as HelloServer).user.IP = this._socket.inetAddress.toString()
                this.addToView((msg as HelloServer).user)
                val selfInfo = UserInfo(Name = DeviceName.getDeviceName(), ID = UUID.randomUUID().toString(), IP = "")
                val greetingMsg = HelloClient(selfInfo)
                this.sendMsg(greetingMsg as BaseMessage)
            }
            "chat-message" -> ConnectionHandler.addChatMsgToMap(this._socket.inetAddress.toString(),
                msg as ChatMessage)
        }
    }

}

