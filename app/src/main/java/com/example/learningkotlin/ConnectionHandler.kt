package com.example.learningkotlin

import TcpClientSocket
import TcpServerSocket
import UdpSocket
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var activity: AppCompatActivity

    init {
        _udpSocket = UdpSocket(3000/*TODO: think about this later*/)
        _udpSocket?.setConnectionHandler(this)
//        _tcpServerSocket = TcpServerSocket(4000)
//        _tcpClientSocket.add(TcpClientSocket(InetAddress.getByName("192.168.1.147"), 4000))
    }

    fun findUsers() {
        val worker = Runnable { _udpSocket?.findUsers() }
        threadPool.run(worker)
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
        _tcpClientSocket[0].setTxtView(this.textview, this.activity)

        this.activity.runOnUiThread {
            Toast.makeText(activity.applicationContext, "this phone is client", Toast.LENGTH_LONG).show()
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
        _tcpServerSocket?.setTxtView(this.textview, this.activity)
        this.activity.runOnUiThread {
            Toast.makeText(activity.applicationContext, "this phone is server", Toast.LENGTH_LONG).show()
        }
    }

    fun sendMsg(msg: BaseMessage) {
        //TODO: fix this later
        _tcpServerSocket?.sendMsg(msg)
        if(_tcpClientSocket.isNotEmpty())
            _tcpClientSocket[0].sendMsg(msg)
    }

    //temp function
    fun setTxtView(txtView: TextView, activity: AppCompatActivity) {
        this.textview = txtView
        this.activity = activity
        _udpSocket?.setTxtView(this.textview, this.activity)
    }

}