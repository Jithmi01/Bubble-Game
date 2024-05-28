package com.example.game3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<Button>(R.id.start).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        findViewById<View>(R.id.level1).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}