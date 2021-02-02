package com.homework.androidcourseproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class WelcomeActivity : AppCompatActivity(){
//    , ValueEventListener  {
//    private var mAuth: FirebaseAuth? = null
//    private var currentUser: FirebaseUser? = null
//    private lateinit var dataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

//        initialize()

//        if (currentUser != null){openMap()}

        val thread = Thread(Runnable {
            Thread.sleep(1000)
            val welcomeIntent = Intent(this, SignInActivity::class.java)
            startActivity(welcomeIntent)
            finish()
        })
        thread.start()
    }

//    private fun openMap() {
//        dataBaseReference.child("Users").addValueEventListener(ValueEventListener)
//
//    }

//    override fun onDataChange(snapshot: DataSnapshot) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onCancelled(error: DatabaseError) {
//        TODO("Not yet implemented")
//    }

//    private fun initialize() {
//        mAuth = FirebaseAuth.getInstance()
//        currentUser = mAuth?.currentUser
//        dataBaseReference = FirebaseDatabase.getInstance().reference.child("Users")
//    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}