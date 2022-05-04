package com.example.learningkotlin

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

        }
        threadPool.run(worker)
    }

    fun setBugReport(reportStatus: Boolean) {
        this.report = reportStatus
        this.log("I am running")
    }

    fun log(msg: String) {
        if(!report)
            return
        val worker = Runnable {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            var currentDate = sdf.format(Date())
            currentDate += " = "
            currentDate += msg
            val snd = DatagramPacket(currentDate.toByteArray(), currentDate.length, InetAddress.getByName("192.168.1.147"), 3500);
            socket.send(snd)
        }
        threadPool.run(worker)
    }
}