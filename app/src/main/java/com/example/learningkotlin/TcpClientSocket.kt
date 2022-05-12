import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.learningkotlin.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread

class TcpClientSocket(ipAddress: InetAddress, portNumber: Int,
                      val addToView: (user: UserInfo) -> Unit) {
    private lateinit var _socket: Socket
    private lateinit var _outputStream: DataOutputStream
    private lateinit var _inputStream: DataInputStream
    private var _receiverBool: Boolean = false
    /*temp section*/
    var cHandler: ConnectionHandler? = null

    init {
        val worker = Runnable {
            this._socket = Socket(ipAddress, portNumber)
            this._outputStream = DataOutputStream(_socket.getOutputStream())
            this._inputStream = DataInputStream(_socket.getInputStream())
            this._receiverBool = true
            thread {
                this.receive()
            }
        }
        threadPool.run(worker)
    }

    private fun receive() {
        while (this._receiverBool) {
            lateinit var deserialized: BaseMessage
            try {
                var message = _inputStream.readUTF()
                val json = Json { encodeDefaults = true }
                deserialized = json.decodeFromString<BaseMessage>(message)
            }
            catch (e: kotlinx.serialization.SerializationException) {
                //TODO: change print to logcat
                println("error in deserialization ${e.message}")
                continue
            }
            catch (e: java.io.EOFException) {
                //TODO: change print to logcat
                println("client disconnected ${e.message}")
                //TODO: make it work for every client
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
            "greetings-client" -> this.addToView((msg as HelloClient).user)
        }
    }

    fun sendMsg(msg: BaseMessage) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            _outputStream.writeUTF(serialized)
        }
        threadPool.run(worker)
    }

    fun destroyObject() {
        this._socket.close()
        this._inputStream.close()
        this._outputStream.close()
        this._receiverBool = false
    }
}