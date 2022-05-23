package com.example.learningkotlin

import TcpClientSocket
import TcpServerSocket
import UdpSocket
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.jaredrummler.android.device.DeviceName
import java.io.IOException
import java.net.InetAddress
import java.util.*
import javax.crypto.SecretKey

class ConnectionHandler {
    var _udpSocket: UdpSocket? = null
    //TODO: handle multiple clients
    private var _tcpClientSocket = mutableMapOf<String, TcpClientSocket>()
    private var _tcpServerSocket: TcpServerSocket? = null
    /*temp section*/
    private lateinit var textview: TextView
    private lateinit var fragment: Fragment
    private lateinit var userViewModel: UsersViewModel
    private lateinit var chatViewModel: ChatViewModel
    //temp argument
    private var selfId: Int = 0
    var otherId: Int = 0

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
    fun setSelfId(id: Int) {
        this.selfId = id
    }

    fun getSelfId(): Int {
        return this.selfId
    }


    fun addClient(ipAddr: InetAddress, keys: Triple<SecretKey, SecretKey, ByteArray>) {
        lateinit var socket: TcpClientSocket
        try {
            socket = TcpClientSocket(ipAddr, 4000, keys, { user: UserInfo ->
                this.addUserToViewModel(user)
            }) { ip: String -> this.removeFromClientsMap(ip) }
            //TODO: move this to constructor
            val selfInfo = UserInfo(Name = DeviceName.getDeviceName(), ID = UUID.randomUUID().toString(), IP = "")
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
        ConnectionHandler.addUserToConnectedList(user)
        this.userViewModel.addData(user)
    }

    fun removeFromClientsMap(ip: String) {
        this._tcpClientSocket.remove(ip)
    }

    companion object {
        private var connectedUsers = mutableSetOf<UserInfo>()
        private var usersMsgList = mutableMapOf<String, MutableLiveData<List<ChatMessage>>>()

        fun getUser(ID: String) : UserInfo? {
            for (user in connectedUsers)
                if(user.ID == ID)
                    return user
            return null
        }

        fun addUserToConnectedList(newUser: UserInfo) {
            for(user in connectedUsers)
                if(user.ID == newUser.ID)
                    return

            connectedUsers.add(newUser)
        }

        fun addChatMsgToMap(IP: String, chatMsg: ChatMessage) {

            if(!usersMsgList.contains(IP))
                usersMsgList[IP] = MutableLiveData(mutableListOf(chatMsg))
            else {
                val currentValue = usersMsgList[IP]?.value as MutableList<ChatMessage>
                currentValue.add(chatMsg)
                usersMsgList[IP]?.postValue(currentValue)
            }
        }

        fun getMsgList(IP: String):  MutableLiveData<List<ChatMessage>>?{
            if(!usersMsgList.contains(IP))
                usersMsgList[IP] = MutableLiveData(mutableListOf())
            return usersMsgList[IP]
        }
    }

}