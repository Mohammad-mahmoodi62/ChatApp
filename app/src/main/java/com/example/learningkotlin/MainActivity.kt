package com.example.learningkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var test:MessageSender ?= null

        thread {
            test = MessageSender("192.168.1.147", 54010)
        }

        val changeBtn = findViewById<Button>(R.id.changetext)
        // set on-click listener
        changeBtn.setOnClickListener {
            // your code to perform when the user clicks on the button
            val userinput = findViewById<EditText>(R.id.userinput)
            var userMsg:String = userinput.text.toString()
            thread {
                test?.send_message(userMsg)
            }
        }
    }
}

