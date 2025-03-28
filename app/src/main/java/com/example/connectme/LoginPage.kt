package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.connectme.FirebaseConsts.USER_PATH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginPage : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var registerText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        auth = FirebaseAuth.getInstance()
        email = findViewById(R.id.Username_login)
        password = findViewById(R.id.Password_login)
        loginButton = findViewById(R.id.LoginButton_login)
        forgotPassword = findViewById(R.id.ForgotPassword_login)
        registerText = findViewById(R.id.Register_login)

        loginButton.setOnClickListener { loginUser() }
        registerText.setOnClickListener {
            startActivity(Intent(this, RegisterPage::class.java))
        }

    }

    override fun onStart() {
        super.onStart()
        // If the user is already logged in, go directly to the MainFeedScreen
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainFeedScreen::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainFeedScreen::class.java))
                    finish()
                    setToken()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            if(it.isNotEmpty())
            {
                FirebaseDatabase.getInstance().getReference(USER_PATH)
                    .child(FirebaseAuth.getInstance().uid!!)
                    .updateChildren(mapOf("token" to it))
            }
        }

    }
}