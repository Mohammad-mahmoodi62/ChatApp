package com.example.learningkotlin
import java.net.*
import java.io.DataOutputStream
import java.io.IOException

class MessageSender {
    private var socket_:Socket ?= null
    private var ops_:DataOutputStream ?= null

    constructor(ip:String, port:Int)
    {
        try {
            socket_ = Socket(ip, port)
            ops_ = DataOutputStream(socket_?.getOutputStream())
        } catch (u: UnknownHostException) {
            System.out.println(u)
        } catch (i: IOException) {
            println(i)
        }
    }

    public fun send_message(message: String)
    {
        ops_?.writeUTF(message)
    }
}