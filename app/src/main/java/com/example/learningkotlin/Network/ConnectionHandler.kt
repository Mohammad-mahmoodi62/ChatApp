package com.example.learningkotlin

import KeyAgreement
import TcpClientSocket
import TcpServerSocket
import UdpSocket
import androidx.lifecycle.MutableLiveData
import com.jaredrummler.android.device.DeviceName
import kotlinx.serialization.SerialName
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.util.*
import javax.crypto.SecretKey

object ConnectionHandler {
    var _udpSocket: UdpSocket? = null
    //TODO: handle multiple clients
    private var _tcpClientSocket = mutableMapOf<String, TcpClientSocket>()
    private var _tcpServerSocket: TcpServerSocket? = null
    private lateinit var _addFoundUser: (user: UserInfo) -> Unit
    private val _keyAgreement: KeyAgreement
    var identifiedUsers = mutableMapOf<String, InetAddress>()
    private lateinit var addConnectedUserToViewModel: (user: UserInfo) -> Unit
    private lateinit var updateConnectedUser: (newUserID: String, lastMsg: String) -> Unit

    //temp
    lateinit var selfID: String

    init {
        _udpSocket = UdpSocket(3000/*TODO: think about this later*/) {
                msg: BaseMessage, dp: DatagramPacket -> this.handleReceivedMsg(msg, dp)
        }
        this._keyAgreement = KeyAgreement()

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

    private fun handleReceivedMsg(msg:BaseMessage, dp: DatagramPacket) {
        BugRepoter.log("message type ${msg.javaClass.getAnnotation(SerialName::class.java).value} received")
        when (msg.javaClass.getAnnotation(SerialName::class.java).value) {
            "identify-your-self-msg" -> this.identifySelf(dp)
            "identification-msg" -> this.handleIdentificationMsg(dp, msg as HereIAm)
            "alice-public-key" -> {
                this.handleAlicePubKey(dp, msg as AlicePubKey)
            }
            "bob-public-key" -> {
                this.handleBobPubKey(msg as BobPubKey, dp)
            }
        }
    }

    private fun identifySelf(dp: DatagramPacket) {
        val selfUser = UserInfo(Name = DeviceName.getDeviceName(), ID = UUID.randomUUID().toString(), IP = "")
        this.selfID = selfUser.ID
        val msg = HereIAm(selfUser)
        this._udpSocket?.sendMsg(msg as BaseMessage, dp)
    }

    private fun handleBobPubKey(bpk: BobPubKey, dp: DatagramPacket) {
        val keys = this._keyAgreement.aliceGenerateNeededKeys(bpk.pubKey)
        this.addClient(dp.address, keys)
    }

    private fun handleAlicePubKey(dp: DatagramPacket, apk: AlicePubKey) {
        BugRepoter.log("I am Server")
        val msg = BobPubKey(this._keyAgreement.generateBobPublicKey(apk.pubKey))
        this._udpSocket?.sendMsg(msg as BaseMessage, dp)

        val keys = this._keyAgreement.bobGenerateNeededKeys()
        ClientHandler.clientKeys[dp.address.toString()] = keys
    }

    private fun handleIdentificationMsg(dp: DatagramPacket, user: HereIAm) {
        this.identifiedUsers[user.Info.ID] = dp.address

        this._addFoundUser(user.Info)
    }

    fun findUsers() {
        val worker = Runnable { _udpSocket?.findUsers() }
        threadPool.run(worker)
    }

    //temp function
    fun setAddUserToViewModelFun(func: (user: UserInfo) -> Unit) {
        this.addConnectedUserToViewModel = func
    }

    fun connectToUser(user: UserInfo) {
        this.sendAlicePubKey(this.identifiedUsers[user.ID]!!)
        this.identifiedUsers.remove(user.ID)
    }

    fun connectToUser(ip: String) {
        this.sendAlicePubKey(InetAddress.getByName(ip))
    }

    private fun sendAlicePubKey(ipAddress: InetAddress) {
        BugRepoter.log("I am client")
        val worker = Runnable {
            val msg = AlicePubKey(this._keyAgreement.generateAlicePublicKey())
            this._udpSocket?.sendMsg(msg as BaseMessage, ipAddress)
        }
        threadPool.run(worker)
    }


    private fun addClient(ipAddr: InetAddress, keys: Triple<SecretKey, SecretKey, ByteArray>) {
        lateinit var socket: TcpClientSocket
        try {
            socket = TcpClientSocket(ipAddr, 4000, keys, { user: UserInfo ->
                this.addUserToViewModel(user)
            }) { ip: String -> this.removeFromClientsMap(ip) }
            //TODO: move this to constructor
            val selfInfo = UserInfo(Name = DeviceName.getDeviceName(), ID = UUID.randomUUID().toString(), IP = "")
            this.selfID = selfInfo.ID
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

    fun sendMsg(msg: BaseMessage, ipAddr: String): Boolean {
        return if(ClientHandler.clientHandlers.contains(ipAddr)) {
            ClientHandler.clientHandlers[ipAddr]?.sendMsg(msg)
            true
        } else if (this._tcpClientSocket.contains(ipAddr)) {
            this._tcpClientSocket[ipAddr]?.sendMsg(msg)
            true
        } else
            false
    }

    fun setFunAddFoundUser(addUser: (user: UserInfo) -> Unit) {
        this._addFoundUser = addUser
    }

    private fun addUserToViewModel(user: UserInfo) {
        ConnectionHandler.addUserToConnectedList(user)
        this.addConnectedUserToViewModel(user)
    }

    private fun removeFromClientsMap(ip: String) {
        this._tcpClientSocket.remove(ip)
    }

    private var connectedUsers = mutableSetOf<UserInfo>()
    private var usersMsgList = mutableMapOf<String, MutableLiveData<List<ChatMessage>>>()

    fun getUser(ID: String) : UserInfo? {
        for (user in connectedUsers)
            if(user.ID == ID)
                return user
        return null
    }

    private fun addUserToConnectedList(newUser: UserInfo) {
        for(user in connectedUsers)
            if(user.ID == newUser.ID)
                return

        connectedUsers.add(newUser)
    }

    fun addChatMsgToMap(IP: String, chatMsg: ChatMessage) {

        if(!usersMsgList.contains(IP)) {
            usersMsgList[IP] = MutableLiveData(mutableListOf(chatMsg))
        }
        else {
            val currentValue = usersMsgList[IP]?.value as MutableList<ChatMessage>
            currentValue.add(chatMsg)
            usersMsgList[IP]?.postValue(currentValue)
            updateConnectedUser(chatMsg.senderId, chatMsg.message)
        }
    }

    fun getMsgList(IP: String):  MutableLiveData<List<ChatMessage>>?{
        if(!usersMsgList.contains(IP))
            usersMsgList[IP] = MutableLiveData(mutableListOf())
        return usersMsgList[IP]
    }

    fun setUpdateUserFun(func: (newUserID: String, lastMsg: String)  -> Unit) {
        this.updateConnectedUser = func
    }

}