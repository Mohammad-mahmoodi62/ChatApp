package com.example.learningkotlin

import TcpClientSocket
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var socket:Socket
    lateinit var ops:DataOutputStream
    lateinit var ips:DataInputStream
    lateinit var textview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var test = ConnectionHandler()
        textview = findViewById<TextView>(R.id.textView)



        val changeBtn = findViewById<Button>(R.id.changetext)
        val findBtn = findViewById<Button>(R.id.find_users)
        val debugButton = findViewById<Button>(R.id.debugger)
        test.setTxtView(textview, this)
        // set on-click listener
        changeBtn.setOnClickListener {
            // your code to perform when the user clicks on the button
            val userinput = findViewById<EditText>(R.id.userinput)
            var userMsg:String = userinput.text.toString()
//            var msg = OwnedProject2(userMsg, "android")
//            test.sendMsg(msg as BaseMessage)
            var msg = OwnedProject2(userMsg, "android")
//            val json = Json { encodeDefaults = true }
//            val serialized = json.encodeToString(msg as BaseMessage)
            test.sendMsg(msg)
        }
        findBtn.setOnClickListener {
            test.findUsers()
        }
        debugButton.setOnClickListener {
            BugRepoter.setBugReport(true)
        }
    }

    fun receiver() {
        while (true)
        {
            var message = ips.readUTF()
            runOnUiThread {
                textview.text = message
            }
        }
    }
}

