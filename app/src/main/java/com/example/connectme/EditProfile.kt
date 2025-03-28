package com.example.connectme

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class EditProfile : AppCompatActivity() {

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var headerNameText: TextView

    private val PICK_IMAGE_REQUEST = 1
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.Main_profile_pic_editprofile)
        val cameraIcon = findViewById<ImageView>(R.id.cam)
        val doneButton = findViewById<TextView>(R.id.done_edit_profile)

        // Inputs
        nameEditText = findViewById(R.id.nameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        contactEditText = findViewById(R.id.contactEditText)
        headerNameText = findViewById(R.id.headerNameText)

        cameraIcon.setOnClickListener { openGallery() }

        doneButton.setOnClickListener {
            saveProfileDetails()
        }

        loadProfilePicture()
        loadProfileInfo()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageUri?.let {
                val bitmap = uriToBitmap(it)
                profileImageView.setImageBitmap(bitmap)
                val base64Image = encodeToBase64(bitmap)
                saveProfilePicture(base64Image)
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)!!
    }

    private fun encodeToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveProfilePicture(encodedImage: String) {
        currentUserId?.let { userId ->
            databaseRef.child(userId).child("profileImage").setValue(encodedImage)
        }
    }

    private fun loadProfilePicture() {
        currentUserId?.let { userId ->
            databaseRef.child(userId).child("profileImage").get().addOnSuccessListener { snapshot ->
                val encodedImage = snapshot.getValue(String::class.java)
                if (!encodedImage.isNullOrEmpty()) {
                    val decodedByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
                    profileImageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun loadProfileInfo() {
        currentUserId?.let { userId ->
            val userRef = databaseRef.child(userId)
            userRef.child("fullName").get().addOnSuccessListener {
                val name = it.getValue(String::class.java)
                nameEditText.setText(name ?: "")
                headerNameText.text = name ?: ""
            }

            userRef.child("username").get().addOnSuccessListener {
                usernameEditText.setText(it.getValue(String::class.java) ?: "")
            }

            userRef.child("contact").get().addOnSuccessListener {
                contactEditText.setText(it.getValue(String::class.java) ?: "")
            }
        }
    }

    private fun saveProfileDetails() {
        val name = nameEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()

        currentUserId?.let { uid ->
            val userRef = databaseRef.child(uid)
            userRef.child("fullName").setValue(name)
            userRef.child("username").setValue(username)
            userRef.child("contact").setValue(contact)
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
