package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterPage : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        auth = FirebaseAuth.getInstance()

        name = findViewById(R.id.Name_Register)
        username = findViewById(R.id.UserName_Register)
        phone = findViewById(R.id.Phonenum_Register)
        email = findViewById(R.id.Email_Register)
        password = findViewById(R.id.Password_Register)
        registerButton = findViewById(R.id.RegisterButton_Register)
        loginText = findViewById(R.id.Login_Register)

        registerButton.setOnClickListener { registerUser() }
        loginText.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }
    }

    private fun registerUser() {
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainFeedScreen::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
