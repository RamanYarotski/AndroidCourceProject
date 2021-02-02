package com.homework.androidcourseproject

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


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
    private var mAuth: FirebaseAuth? = null
    private lateinit var loadingBar: ProgressDialog
    private var isQuest: Boolean = false
    private lateinit var userDatabaseRef: DatabaseReference
    private lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        init()

        justSignIn.setOnClickListener {
            isQuest = true
            val loginIntent = Intent(this, MainActivity::class.java)
            loginIntent.putExtra("isQuest", isQuest)
            startActivity(loginIntent)
        }

        signInButton.setOnClickListener {
            val name = name.text.toString()
            val password = password.text.toString()
            signIn(name, password)
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

        registrationButton.setOnClickListener {
            val name = name.text.toString()
            val password = password.text.toString()
            register(name, password)
        }

    }

//    override fun onStart() {
//        super.onStart()
//        val currentUser = mAuth.currentUser
//        updateUI(currentUser)
//    }

    private fun register(email: String, password: String) {
        loadingBar.apply {
            setTitle("Registration")
            setMessage("Wait please")
            show()
        }
        mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration success", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                    val loginIntent = Intent(this, MainActivity::class.java)
                    startActivity(loginIntent)
                } else {
                    Toast.makeText(this, "Registration error", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }
            }
    }

    private fun signIn(email: String, password: String) {
        loadingBar.setTitle("Sign in")
        loadingBar.setMessage("Wait please")
        loadingBar.show()

        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    userID = mAuth?.currentUser?.uid.toString()
                    userDatabaseRef = FirebaseDatabase.getInstance()
                        .reference.child("Users").child(userID)
                    userDatabaseRef.setValue(true)

                    val loginIntent = Intent(this, MainActivity::class.java)
                    loginIntent.putExtra("userID", userID)
                    startActivity(loginIntent)

                    Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()

                } else {
                    Toast.makeText(this, "Sign in error", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }
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
        mAuth = FirebaseAuth.getInstance()

        loadingBar = ProgressDialog(this)
    }

}