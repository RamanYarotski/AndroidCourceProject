package com.homework.androidcourseproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val thread = Thread(Runnable {
            Thread.sleep(1000)
            val welcomeIntent = Intent(this, SignInActivity::class.java)
            startActivity(welcomeIntent)
            finish()
        })
        thread.start()
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}