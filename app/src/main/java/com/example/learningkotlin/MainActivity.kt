package com.example.learningkotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.jaredrummler.android.device.DeviceName
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {

    var test: ConnectionHandler? = null
    lateinit var toolbar: Toolbar
    lateinit var toolbarText: TextView
    lateinit var toolbarImage: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        DeviceName.init(this)
        this.test = ConnectionHandler()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.updateToolBar()

    }

    fun updateToolBar() {
        this.toolbar = findViewById(R.id.toolBar)
        this.toolbarText = findViewById(R.id.user_name)
        this.toolbarImage = findViewById(R.id.circleImageView)

        toolbar.setTitleTextColor(resources.getColor(R.color.secondary_color))
        setSupportActionBar(toolbar)

        this.toolbarText.text = ""
        //TODO add self image

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val builder = AppBarConfiguration.Builder(navController.graph)
        val appBarConfiguration = builder.build()
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}

