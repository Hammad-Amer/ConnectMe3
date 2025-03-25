package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterPage : AppCompatActivity() {

    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var phone: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var loginText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference  // Realtime Database Reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")  // Reference to "Users" in Realtime Database

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
        val userName = username.text.toString().trim()
        val userEmail = email.text.toString().trim()
        val userPassword = password.text.toString().trim()
        val fullName = name.text.toString().trim()
        val phoneNumber = phone.text.toString().trim()

        if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val userMap = mapOf(
                            "username" to userName,
                            "email" to userEmail,
                            "fullName" to fullName,
                            "phone" to phoneNumber
                        )

                        database.child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainFeedScreen::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

