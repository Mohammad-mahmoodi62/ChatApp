package com.example.learningkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


class MainActivity : AppCompatActivity() {

    var test: ConnectionHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        this.test = ConnectionHandler()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}

