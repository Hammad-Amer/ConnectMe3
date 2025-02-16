package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)


        val RegisterButton = findViewById<Button>(R.id.RegisterButton_Register)
        RegisterButton.setOnClickListener {
            val intent = Intent(this, MainFeedScreen::class.java)
            startActivity(intent)
            finish()
        }


        val LoginText = findViewById<TextView>(R.id.Login_Register)
        LoginText.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }
}