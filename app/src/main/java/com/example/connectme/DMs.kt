package com.example.connectme

import RecyclerItemClickListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DMs : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterDMs
    private val dmList = mutableListOf<ModelDMs>()
    private val database = FirebaseDatabase.getInstance().reference.child("Chats")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dms)

        recyclerView = findViewById(R.id.recyclerView_Dms)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterDMs(dmList)
        recyclerView.adapter = adapter

        fetchChatUsers()

        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(this, recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val selectedUserId = dmList[position].userId // Get selected user's ID
                        val intent = Intent(this@DMs, ChatScreen::class.java)
                        intent.putExtra("USER_ID", selectedUserId) // Pass user ID dynamically
                        startActivity(intent)
                    }

                    override fun onLongItemClick(position: Int) {}
                })
        )
    }

    private fun fetchChatUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        val uniqueUsers = HashSet<String>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key.toString()
                    if (chatId.contains(currentUserId)) {
                        for (message in chatSnapshot.child("messages").children) {
                            val senderId = message.child("senderId").value.toString()
                            val receiverId = message.child("receiverId").value.toString()

                            if (senderId == currentUserId) {
                                uniqueUsers.add(receiverId)
                            } else if (receiverId == currentUserId) {
                                uniqueUsers.add(senderId)
                            }
                        }
                    }
                }
                fetchUsernames(uniqueUsers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load chats", error.toException())
            }
        })
    }

    private fun fetchUsernames(userIds: Set<String>) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        dmList.clear()



        for (userId in userIds) {
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").value?.toString() ?: "Unknown"
                    val profileImageString = snapshot.child("profileImage").value?.toString() ?: ""
                    dmList.add(ModelDMs(R.drawable.pf6, username, userId, profileImageString))
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to load usernames", error.toException())
                }
            })
        }
    }
}
