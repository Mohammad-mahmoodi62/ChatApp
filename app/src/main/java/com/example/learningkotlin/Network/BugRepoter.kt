package com.example.learningkotlin

import android.annotation.SuppressLint
import com.jaredrummler.android.device.DeviceName
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*

object BugRepoter {
    var report: Boolean = false
    lateinit var socket: DatagramSocket
    init {
        val worker = Runnable {
            socket = DatagramSocket(35000)
            this.report = true

        }
        threadPool.run(worker)
    }

    fun setBugReport(reportStatus: Boolean) {
        this.report = reportStatus
        this.log("I am running")
    }

    @SuppressLint("SimpleDateFormat")
    fun log(msg: String) {
        if(!report)
            return
        val worker = Runnable {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            var sendMsg = "{${DeviceName.getDeviceName()}}[${sdf.format(Date())}]: $msg"
            val snd = DatagramPacket(
                sendMsg.toByteArray(),
                sendMsg.length,
                InetAddress.getByName("192.168.1.147"),
                3500
            );
            socket.send(snd)
        }
        threadPool.run(worker)

    }
}