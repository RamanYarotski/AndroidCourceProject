package com.homework.androidcourseproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {
    private lateinit var signInText: TextView
    private lateinit var registrationText: TextView
    private lateinit var name: EditText
    private lateinit var password: EditText
    private lateinit var signInButton: Button
    private lateinit var registrationButton: Button
    private lateinit var registrationCheck: TextView
    private lateinit var signInCheck: TextView
    private lateinit var justSignIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        init()

        justSignIn.setOnClickListener {
            val loginIntent = Intent(this, MainActivity::class.java)
            startActivity(loginIntent)
        }

        signInButton.setOnClickListener {
            val loginIntent = Intent(this, MainActivity::class.java)
            startActivity(loginIntent)
        }

        registrationCheck.setOnClickListener {
            signInText.visibility = View.INVISIBLE
            signInButton.visibility = View.INVISIBLE
            registrationCheck.visibility = View.INVISIBLE
            registrationText.visibility = View.VISIBLE
            registrationButton.visibility = View.VISIBLE
            signInCheck.visibility = View.VISIBLE
        }

        signInCheck.setOnClickListener {
            signInText.visibility = View.VISIBLE
            signInButton.visibility = View.VISIBLE
            registrationCheck.visibility = View.VISIBLE
            registrationText.visibility = View.INVISIBLE
            registrationButton.visibility = View.INVISIBLE
            signInCheck.visibility = View.INVISIBLE
        }
    }

    private fun init() {
        signInText = findViewById(R.id.signInText)
        registrationText = findViewById(R.id.registrationText)
        name = findViewById(R.id.emailInSignInEditText)
        password = findViewById(R.id.passwordEditText)
        signInButton = findViewById(R.id.signInButton)
        registrationButton = findViewById(R.id.registrationButton)
        registrationCheck = findViewById(R.id.registrationCheck)
        signInCheck = findViewById(R.id.signInCheck)
        justSignIn = findViewById(R.id.justSignInText)
    }

}