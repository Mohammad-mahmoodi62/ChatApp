import com.example.learningkotlin.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception
import java.net.InetAddress
import java.net.Socket
import javax.crypto.SecretKey
import kotlin.concurrent.thread

class TcpClientSocket(
    val ipAddress: InetAddress, portNumber: Int,
    private val _keys: Triple<SecretKey, SecretKey, ByteArray>,
    val addToView: (user: UserInfo) -> Unit,
    val removeFromMap: (String) -> Unit
) {
    private lateinit var _socket: Socket
    private lateinit var _outputStream: DataOutputStream
    private lateinit var _inputStream: DataInputStream
    private lateinit var _security: Security
    /*temp section*/
    var cHandler: ConnectionHandler? = null

    init {
        val worker = Runnable {
            this._socket = Socket(ipAddress, portNumber)
            this._outputStream = DataOutputStream(_socket.getOutputStream())
            this._inputStream = DataInputStream(_socket.getInputStream())
            this._security = Security(this._keys.first, this._keys.second, this._keys.third)
            if(!this::_security.isInitialized)
                BugRepoter.log("security not initialized")
            else
                BugRepoter.log("security initialized")
            thread {
                this.receive()
            }
        }
        threadPool.run(worker)
    }

    private fun receive() {
        var received = ByteArray(8192)
        while (this._socket.isConnected) {
            lateinit var deserialized: BaseMessage
            try {
                //TODO:if possible chage it to readUTF()
                val bytesRead = this._inputStream.read(received)
                if(bytesRead < 0) {
                    this.destroyObject()
                    break
                }
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
                //TODO: make it work for every client
                this.destroyObject()
                break
            }
            catch (e: MacException) {
                BugRepoter.log("error occurred in receiving message ${e.message}")
                continue
            }

            catch (e: Exception) {
                BugRepoter.log("error occurred in receiving message ${e.message}")
                this.destroyObject()
                break
            }

            this.handleReceivedMsg(deserialized)
        }
    }

    private fun handleReceivedMsg(msg: BaseMessage) {

        when(msg.javaClass.getAnnotation(SerialName::class.java).value) {
//            "sam-service" -> this.activity.runOnUiThread{
//                this.textview.text = (msg as OwnedProject2).msg
//            }
//            "chat-message" -> this.cHandler?.addChatToViewModel(msg as ChatMessage)
            "greetings-client" -> {
                (msg as HelloClient).user.IP = this.ipAddress.toString()
                this.addToView((msg as HelloClient).user)
            }
            "chat-message" -> ConnectionHandler.addChatMsgToMap(this.ipAddress.toString(),
                                                                msg as ChatMessage)
        }
    }

    fun sendMsg(msg: BaseMessage) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)

            val macEncrypted = this._security.encryptAddHmac(serialized)
            _outputStream.write(macEncrypted, 0, macEncrypted.size)
        }
        threadPool.run(worker)
    }

    fun destroyObject() {
        this.removeFromMap(this._socket.inetAddress.toString())
        this._socket.close()
        this._inputStream.close()
        this._outputStream.close()
    }
}