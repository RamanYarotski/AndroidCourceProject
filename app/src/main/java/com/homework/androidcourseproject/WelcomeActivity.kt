package com.homework.androidcourseproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val thread = Thread(Runnable {
            Thread.sleep(4000)
            val welcomeIntent = Intent(this, MainActivity::class.java)
            startActivity(welcomeIntent)
        })
        thread.start()
    }
}