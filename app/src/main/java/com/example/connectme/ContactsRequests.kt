package com.example.connectme

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactsRequests : AppCompatActivity() {

    private lateinit var adapter: AdapterRequests
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contacts_requests)

        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.requestsRecyclerView)

        adapter = AdapterRequests(mutableListOf(),
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchRequests()
    }

    private fun fetchRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val requestsRef = FirebaseDatabase.getInstance().getReference("Requests").child(currentUserId).child("pending")

        requestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestsList = mutableListOf<ModelRequest>()
                for (data in snapshot.children) {
                    val userId = data.key // The userId of the sender
                    val username = data.child("username").getValue(String::class.java) ?: ""
                    val profileImageUrl = data.child("profileImageUrl").getValue(String::class.java) ?: ""

                    if (userId != null) {
                        requestsList.add(ModelRequest(userId, username, profileImageUrl))
                    }
                }
                adapter.updateList(requestsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ContactsRequests", "Database Error: ${error.message}")
            }
        })
    }

    private fun acceptRequest(request: ModelRequest) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")

        usersRef.child(currentUserId).child("followers").child(request.userId).setValue(true)

        usersRef.child(request.userId).child("following").child(currentUserId).setValue(true)

        FirebaseDatabase.getInstance().getReference("Requests")
            .child(currentUserId).child("pending").child(request.userId)
            .removeValue()
    }

    private fun rejectRequest(request: ModelRequest) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference("Requests")
            .child(currentUserId).child("pending").child(request.userId)
            .removeValue()
            .addOnSuccessListener {
                adapter.removeItem(request)
            }
            .addOnFailureListener { error ->
                Log.e("ContactsRequests", "Failed to reject request: ${error.message}")
            }
    }
}
