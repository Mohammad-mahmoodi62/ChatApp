package com.example.learningkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jaredrummler.android.device.DeviceName


class MainActivity : AppCompatActivity() {

    var test: ConnectionHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DeviceName.init(this)
        this.test = ConnectionHandler()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}

