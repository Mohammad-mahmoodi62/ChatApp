package com.example.learningkotlin

import TcpClientSocket
import TcpServerSocket
import UdpSocket
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.serialization.SerialName
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class ConnectionHandler {
    var _udpSocket: UdpSocket? = null
    //TODO: handle multiple clients
    var _tcpClientSocket = mutableListOf<TcpClientSocket>()
    var _tcpServerSocket: TcpServerSocket? = null
    /*temp section*/
    private lateinit var textview: TextView
    private lateinit var fragment: Fragment
    private lateinit var userViewModel: UsersViewModel
    private lateinit var chatViewModel: ChatViewModel
    //temp argument
    private var selfId: Int = 0
    var otherId: Int = 0
    var rand = false

    init {
        _udpSocket = UdpSocket(3000/*TODO: think about this later*/)
        _udpSocket?.setConnectionHandler(this)
//        _tcpServerSocket = TcpServerSocket(4000)
//        _tcpClientSocket.add(TcpClientSocket(InetAddress.getByName("192.168.1.147"), 4000))
        //temp
        this.selfId = (0..100).random()
        this.otherId = (0..100).random()
    }

    fun findUsers() {
//        val msg = "random user ${(0..100).random()}"
//        this.addUserToViewModel((0..100).random())
//        return
        val worker = Runnable { _udpSocket?.findUsers() }
        threadPool.run(worker)
    }

    //temp function
    fun setUserViewModel(vh: UsersViewModel) {
        this.userViewModel = vh
    }

    //temp function
    fun setChatViewModel(vh: ChatViewModel) {
        this.chatViewModel = vh
    }

    //temp function
    fun addUserToViewModel(id: Int) {
        this.userViewModel.addData(id.toString())
    }

    //temp function
    fun addChatToViewModel(msg:ChatMessage) {
        this.chatViewModel.addData(msg)
    }

    //temp function
    fun setSelfId(id: Int) {
        this.selfId = id
    }

    fun getSelfId(): Int {
        return this.selfId
    }


    fun addClient(ipAddr: InetAddress) {
        BugRepoter.log("we reached in add client socket")
        lateinit var socket: TcpClientSocket
        try {
            socket = TcpClientSocket(ipAddr, 4000)
        }
        catch (e: IOException) {
            BugRepoter.log("IO error when creating server socket ${e.message}")
        }
        catch (e: SecurityException) {
            BugRepoter.log("SecurityException error when creating server socket ${e.message}")
        }
        catch (e: IllegalArgumentException) {
            BugRepoter.log("IllegalArgumentException error when creating server socket ${e.message}")
        }
        catch (e: NullPointerException) {
            BugRepoter.log("NullPointerException error when creating server socket ${e.message}")
        }
        _tcpClientSocket.add(socket)
//        _tcpClientSocket[0].setTxtView(this.textview, this.activity)

        this._tcpClientSocket[0].cHandler = this

        this.fragment.activity?.runOnUiThread {
            Toast.makeText(this.fragment.context, "this phone is client and self id is ${this.selfId}", Toast.LENGTH_LONG).show()
        }
    }

    fun startServer() {
        BugRepoter.log("we reached in add server socket")
        try {
            _tcpServerSocket = TcpServerSocket(4000)
        }
        catch (e: IOException) {
            BugRepoter.log("IO error when creating server socket ${e.message}")
        }
        catch (e: SecurityException) {
            BugRepoter.log("SecurityException error when creating server socket ${e.message}")
        }
        catch (e: IllegalArgumentException) {
            BugRepoter.log("IllegalArgumentException error when creating server socket ${e.message}")
        }
//        _tcpServerSocket?.setTxtView(this.textview, this.activity)
        this.fragment.activity?.runOnUiThread {
            Toast.makeText(this.fragment.context, "this phone is server and self id is ${this.selfId}", Toast.LENGTH_LONG).show()
        }
        this._tcpServerSocket?.cHandler = this
    }

    fun sendMsg(msg: BaseMessage) {
//        var msg: ChatMessage
//
//        if(rand) {
//            msg = ChatMessage("random message ${(0..100).random()}", this.selfId.toString(),
//                                this.otherId.toString())
//        } else {
//            msg = ChatMessage("random message ${(0..100).random()}", this.otherId.toString(),
//                this.selfId.toString())
//        }
//        rand = !rand
//        this.addChatToViewModel(msg)
//        return
        //TODO: fix this later
        if (msg.javaClass.getAnnotation(SerialName::class.java).value == "chat-message")
            this.addChatToViewModel(msg as ChatMessage)
        _tcpServerSocket?.sendMsg(msg)
        if(_tcpClientSocket.isNotEmpty())
            _tcpClientSocket[0].sendMsg(msg)
    }

    //temp function
    fun setFragment(fragment: Fragment) {
        this.fragment = fragment
    }

}