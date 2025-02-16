package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)


        val loginButton = findViewById<Button>(R.id.LoginButton_login)
        loginButton.setOnClickListener {
            val intent = Intent(this, MainFeedScreen::class.java)
            startActivity(intent)
            finish()
        }


        val registerText = findViewById<TextView>(R.id.Register_login)
        registerText.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }
    }
}