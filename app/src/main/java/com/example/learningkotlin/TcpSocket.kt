import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.learningkotlin.BaseMessage
import com.example.learningkotlin.OwnedProject2
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.*
import kotlin.concurrent.thread

class TcpServerSocket (portNumber: Int) {
    private lateinit var _socket: ServerSocket
    private lateinit var _clientSocket: Socket
    private lateinit var _outputStream: DataOutputStream
    private lateinit var _inputStream: DataInputStream
    private var _receiverBool: Boolean = false
    /*temp section*/
    private lateinit var textview: TextView
    private lateinit var activity: AppCompatActivity

    init {
        //TODO: make it work for every client
        val worker = Runnable {
            this._socket = ServerSocket(portNumber)
            this._clientSocket = this._socket.accept()
            this._outputStream = DataOutputStream(this._clientSocket.getOutputStream())
            this._inputStream = DataInputStream(this._clientSocket.getInputStream())
            this._receiverBool = true
            val worker = Runnable {
                this.receive()
            }
            threadPool.run(worker)
        }
        threadPool.run(worker)
    }

    private fun receive() {
        while (this._receiverBool) {
            lateinit var deserialized: BaseMessage
            try {
                var message = _inputStream?.readUTF()
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

    fun setTxtView(txtView: TextView, activity: AppCompatActivity) {
        this.textview = txtView
        this.activity = activity
    }

    private fun handleReceivedMsg(msg:BaseMessage) {
        when(msg.javaClass.getAnnotation(SerialName::class.java).value) {
            "sam-service" -> this.activity.runOnUiThread{
                this.textview.text = (msg as OwnedProject2).msg
            }
        }
    }

    fun sendMsg(msg: BaseMessage) {
        val worker = Runnable {
            val json = Json { encodeDefaults = true }
            val serialized = json.encodeToString(msg)
            _outputStream?.writeUTF(serialized)
        }
        threadPool.run(worker)
    }

    fun destroyObject() {
        this._socket.close()
        this._clientSocket.close()
        this._inputStream.close()
        this._outputStream.close()
        this._receiverBool = false
    }
}

class TcpClientSocket(ipAddress: InetAddress, portNumber: Int) {
    private lateinit var _socket: Socket
    private lateinit var _outputStream: DataOutputStream
    private lateinit var _inputStream: DataInputStream
    private var _receiverBool: Boolean = false
    /*temp section*/
    private lateinit var textview: TextView
    private lateinit var activity: AppCompatActivity

    init {
        val worker = Runnable {
            this._socket = Socket(ipAddress, portNumber)
            this._outputStream = DataOutputStream(_socket.getOutputStream())
            this._inputStream = DataInputStream(_socket.getInputStream())
            this._receiverBool = true
            val worker = Runnable {
                this.receive()
            }
            threadPool.run(worker)
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

    private fun handleReceivedMsg(msg:BaseMessage) {

        when(msg.javaClass.getAnnotation(SerialName::class.java).value) {
            "sam-service" -> this.activity.runOnUiThread{
                this.textview.text = (msg as OwnedProject2).msg
            }
        }
    }

    /*temp function*/
    fun setTxtView(txtView: TextView, activity: AppCompatActivity) {
        this.textview = txtView
        this.activity = activity
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