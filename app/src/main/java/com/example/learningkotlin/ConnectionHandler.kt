package com.example.learningkotlin

import TcpClientSocket
import TcpServerSocket
import UdpSocket
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jaredrummler.android.device.DeviceName
import kotlinx.serialization.SerialName
import java.io.IOException
import java.net.InetAddress
import java.net.Socket

class ConnectionHandler {
    var _udpSocket: UdpSocket? = null
    //TODO: handle multiple clients
    var _tcpClientSocket = mutableMapOf<String, TcpClientSocket>()
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

        try {
            this._tcpServerSocket = TcpServerSocket(4000) { user: UserInfo ->
                this.addUserToViewModel(user)
            }
//            this._tcpServerSocket!!.startServer()
            BugRepoter.log("server created")
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
            socket = TcpClientSocket(ipAddr, 4000) { user: UserInfo ->
                this.addUserToViewModel(user)
            }
            //TODO: move this to constructor
            val selfInfo = UserInfo(Name = DeviceName.getDeviceName(), ID = "null")
            val greetingMsg = HelloServer(selfInfo)
            BugRepoter.log("helloServer Sent")
            socket.sendMsg(greetingMsg as BaseMessage)
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
        _tcpClientSocket[ipAddr.toString()] = socket
    }

    fun sendMsg(msg: BaseMessage, ipAddr: String) {
        if(ClientHandler.clientHandlers.contains(ipAddr))
            ClientHandler.clientHandlers[ipAddr]?.sendMsg(msg)
        else
            this._tcpClientSocket[ipAddr]?.sendMsg(msg)
    }

    fun addUserToViewModel(user: UserInfo) {
        this.userViewModel.addData(user.Name)
    }

}